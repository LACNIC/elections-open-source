#!/usr/bin/env python3

from __future__ import annotations

import csv
import io
import os
import re
import shutil
import socket
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parent
REF_SCHEMA = ROOT / "ref" / "elections_schema_old.sql"
REF_TEMPLATE_OLD = ROOT / "ref" / "electionemailtemplate_OLD.sql"
V1_1 = ROOT / "1.1" / "v1.1_script.sql"
V2_0_DDL = ROOT / "2.0" / "v2.0_ddl_script.sql"
V2_0_DATA = ROOT / "2.0" / "v2.0_data_script.sql"
V2_2 = ROOT / "2.2" / "v2.2_script.sql"
V2_3 = ROOT / "2.3" / "v2.3_script.sql"
V2_3_1 = ROOT / "2.3.1" / "v2.3.1_script.sql"


@dataclass
class Finding:
    section: str
    status: str
    message: str


def run_command(cmd: list[str], env: dict[str, str] | None = None, capture: bool = False) -> subprocess.CompletedProcess[str]:
    return subprocess.run(cmd, text=True, capture_output=capture, env=env)


def require_command(name: str) -> str:
    path = shutil.which(name)
    if path:
        return path
    raise RuntimeError(f"No se encontro el binario requerido: {name}")


def parse_schema_dump(path: Path) -> tuple[dict[str, dict[str, str]], set[str]]:
    text = path.read_text()
    tables: dict[str, dict[str, str]] = {}
    create_table_re = re.compile(r"CREATE TABLE public\.(\w+) \((.*?)\);", re.S)
    for table_name, body in create_table_re.findall(text):
        columns: dict[str, str] = {}
        for raw_line in body.splitlines():
            line = raw_line.strip().rstrip(",")
            if not line or line.startswith("--"):
                continue
            match = re.match(r'"?(\w+)"?\s+(.+)', line)
            if match:
                columns[match.group(1)] = line
        tables[table_name] = columns

    sequences = set(re.findall(r"CREATE SEQUENCE public\.(\w+)", text))
    return tables, sequences


def follow_mapping(name: str, mapping: dict[str, str]) -> str:
    current = name
    visited: set[str] = set()
    while current in mapping and current not in visited:
        visited.add(current)
        current = mapping[current]
    return current


def validate_schema() -> list[Finding]:
    findings: list[Finding] = []
    tables, sequences = parse_schema_dump(REF_SCHEMA)
    ddl = V2_0_DDL.read_text() + "\n" + V2_3_1.read_text()

    table_renames = {
        old: new
        for old, new in re.findall(r"ALTER TABLE\s+(?:public\.)?(\w+)\s+RENAME TO\s+(\w+);", ddl)
    }
    dropped_columns = {
        (follow_mapping(table, table_renames), column)
        for table, column in re.findall(r"ALTER TABLE\s+(?:public\.)?(\w+)\s+DROP COLUMN\s+(\w+);", ddl)
    }

    missing_columns: list[str] = []
    for table, old, new in re.findall(r"ALTER TABLE\s+(?:public\.)?(\w+)\s+RENAME COLUMN\s+(\w+)\s+TO\s+(\w+);", ddl):
        final_table = follow_mapping(table, table_renames)
        if (final_table, new) in dropped_columns:
            continue
        if final_table not in tables or new not in tables[final_table]:
            missing_columns.append(f"{final_table}.{new} (desde {table}.{old})")

    missing_added_columns: list[str] = []
    for table, column in re.findall(r"ALTER TABLE\s+(?:public\.)?(\w+)\s+ADD COLUMN\s+(\w+)\s+", ddl):
        final_table = follow_mapping(table, table_renames)
        if final_table not in tables or column not in tables[final_table]:
            missing_added_columns.append(f"{final_table}.{column}")

    dropped_columns_still_present: list[str] = []
    for table, column in re.findall(r"ALTER TABLE\s+(?:public\.)?(\w+)\s+DROP COLUMN\s+(\w+);", ddl):
        final_table = follow_mapping(table, table_renames)
        if final_table in tables and column in tables[final_table]:
            dropped_columns_still_present.append(f"{final_table}.{column}")

    dropped_tables_still_present: list[str] = []
    for table in re.findall(r"DROP TABLE\s+(?:public\.)?(\w+);", ddl):
        if table in tables or follow_mapping(table, table_renames) in tables:
            dropped_tables_still_present.append(table)

    missing_sequences: list[str] = []
    for old, new in re.findall(r"ALTER SEQUENCE\s+public\.(\w+)\s+RENAME TO\s+(\w+);", ddl):
        if new not in sequences:
            missing_sequences.append(f"{new} (desde {old})")

    created_sequences_missing: list[str] = []
    for sequence in re.findall(r"CREATE SEQUENCE\s+public\.(\w+)", ddl):
        if sequence not in sequences:
            created_sequences_missing.append(sequence)

    findings.append(
        Finding(
            "schema",
            "ok" if not missing_columns else "error",
            "Todas las columnas renombradas por v2.0 existen en la referencia."
            if not missing_columns
            else "Columnas finales ausentes respecto a v2.0: " + ", ".join(missing_columns),
        )
    )
    findings.append(
        Finding(
            "schema",
            "ok" if not missing_added_columns else "error",
            "Todas las columnas agregadas por v2.0 existen en la referencia."
            if not missing_added_columns
            else "Columnas agregadas por v2.0 ausentes: " + ", ".join(missing_added_columns),
        )
    )
    findings.append(
        Finding(
            "schema",
            "ok" if not dropped_columns_still_present else "error",
            "Las columnas eliminadas por v2.0 no aparecen en la referencia."
            if not dropped_columns_still_present
            else "Columnas que deberian haberse eliminado y siguen presentes: " + ", ".join(dropped_columns_still_present),
        )
    )
    findings.append(
        Finding(
            "schema",
            "ok" if not dropped_tables_still_present else "error",
            "Las tablas eliminadas por v2.0 no aparecen en la referencia."
            if not dropped_tables_still_present
            else "Tablas que deberian haberse eliminado y siguen presentes: " + ", ".join(dropped_tables_still_present),
        )
    )
    findings.append(
        Finding(
            "schema",
            "ok" if not missing_sequences and not created_sequences_missing else "error",
            "Las secuencias afectadas por v2.0 cierran contra la referencia."
            if not missing_sequences and not created_sequences_missing
            else "Diferencias de secuencias. Faltan: "
            + ", ".join(missing_sequences + created_sequences_missing),
        )
    )

    # Checks puntuales de 1.1, 2.2 y 2.3.
    customization = tables.get("customization", {})
    election = tables.get("election", {})
    vote = tables.get("vote", {})
    uservoter = tables.get("uservoter", {})

    release_specific_checks = [
        ("schema", "ok" if "site_title" in customization and "NOT NULL" in customization["site_title"] else "error", "v1.1 deja customization.site_title como NOT NULL."),
        ("schema", "ok" if "login_title" in customization and "NOT NULL" in customization["login_title"] else "error", "v1.1 deja customization.login_title como NOT NULL."),
        ("schema", "ok" if "show_home" in customization and "NOT NULL" in customization["show_home"] else "error", "v1.1 deja customization.show_home como NOT NULL."),
        ("schema", "ok" if "home_html" in customization else "error", "v1.1 agrega customization.home_html."),
        ("schema", "ok" if "closed" in election and "DEFAULT false" in election["closed"] and "NOT NULL" in election["closed"] else "error", "v2.2 agrega election.closed con DEFAULT false NOT NULL."),
        ("schema", "ok" if "closeddate" in election else "error", "v2.2 agrega election.closeddate."),
        ("schema", "ok" if "uservoter_id" in vote and "NOT NULL" not in vote["uservoter_id"] else "error", "v2.2 deja vote.uservoter_id nullable."),
        ("schema", "ok" if "version" in uservoter else "error", "v2.3 agrega uservoter.version."),
    ]
    findings.extend(Finding(section, status, message) for section, status, message in release_specific_checks)

    return findings


class TempPostgres:
    def __init__(self) -> None:
        self._tmp = tempfile.TemporaryDirectory(prefix="release-validation-")
        self.root = Path(self._tmp.name)
        self.data = self.root / "data"
        self.sock = self.root / "sock"
        self.log = self.root / "postgres.log"
        self.data.mkdir()
        self.sock.mkdir()
        port_socket = socket.socket()
        port_socket.bind(("127.0.0.1", 0))
        self.port = port_socket.getsockname()[1]
        port_socket.close()

        self.env = os.environ.copy()
        pg_bin = Path(require_command("psql")).parent
        self.env["PATH"] = str(pg_bin) + os.pathsep + self.env.get("PATH", "")

    def start(self) -> None:
        initdb = require_command("initdb")
        pg_ctl = require_command("pg_ctl")
        result = run_command([initdb, "-A", "trust", "-U", "postgres", str(self.data)], env=self.env, capture=True)
        if result.returncode != 0:
            raise RuntimeError(result.stderr or result.stdout or "initdb fallo")
        result = run_command(
            [
                pg_ctl,
                "-D",
                str(self.data),
                "-l",
                str(self.log),
                "-o",
                f"-k {self.sock} -p {self.port}",
                "-w",
                "start",
            ],
            env=self.env,
            capture=True,
        )
        if result.returncode != 0:
            raise RuntimeError(result.stderr or result.stdout or "pg_ctl start fallo")

    def stop(self) -> None:
        pg_ctl = require_command("pg_ctl")
        run_command([pg_ctl, "-D", str(self.data), "-w", "stop", "-m", "fast"], env=self.env, capture=True)
        self._tmp.cleanup()

    def psql(self, database: str, *args: str, capture: bool = False) -> subprocess.CompletedProcess[str]:
        psql = require_command("psql")
        cmd = [psql, "-h", str(self.sock), "-p", str(self.port), "-U", "postgres", "-d", database, *args]
        return run_command(cmd, env=self.env, capture=capture)


def fetch_csv_rows(pg: TempPostgres, database: str, query: str) -> list[tuple[str, ...]]:
    statement = f"COPY ({query}) TO STDOUT WITH CSV"
    result = pg.psql(database, "-c", statement, capture=True)
    return [tuple(row) for row in csv.reader(io.StringIO(result.stdout))]


def validate_data_roundtrip() -> list[Finding]:
    findings: list[Finding] = []
    pg = TempPostgres()
    try:
        pg.start()
        for database in ("reference", "roundtrip"):
            create = pg.psql("postgres", "-c", f"CREATE DATABASE {database};", capture=True)
            if create.returncode != 0:
                raise RuntimeError(create.stderr or create.stdout or f"No se pudo crear la base {database}")
            for sql_file in (REF_SCHEMA, REF_TEMPLATE_OLD):
                loaded = pg.psql(database, "-f", str(sql_file), capture=True)
                if loaded.returncode != 0:
                    raise RuntimeError(loaded.stderr or loaded.stdout or f"No se pudo cargar {sql_file}")

        reverse_sql = """
        UPDATE public.parameter SET key='REMITENTE_ESTANDAR' WHERE key='DEFAULT_SENDER';
        UPDATE public.parameter SET key='WS_IPS_HABILITADAS' WHERE key='WS_AUTHORIZED_IPS';
        UPDATE public.parameter SET key='EMAIL_CLAVE' WHERE key='EMAIL_PASSWORD';
        UPDATE public.parameter SET key='EMAIL_USUARIO' WHERE key='EMAIL_USER';
        UPDATE public.parameter
        SET value = REPLACE(value, '/elections', '/elecciones')
        WHERE key='WEBSITE_DEFAULT';
        DELETE FROM public.parameter
        WHERE key IN ('WS_AUTH_METHOD', 'WS_LACNIC_AUTH_URL', 'WS_MAX_PAGE_SIZE', 'AUDIT_REPORT_LINK');

        UPDATE public.electionemailtemplate SET type='NUEVO' WHERE type='NEW';
        UPDATE public.electionemailtemplate SET type='FIRMA' WHERE type='SIGNATURE';
        UPDATE public.electionemailtemplate SET type='COMIENZO_ELECCION' WHERE type='ELECTION_START';
        UPDATE public.electionemailtemplate SET type='AUDITOR_CONFORME' WHERE type='AUDITOR_AGREEMENT';
        UPDATE public.electionemailtemplate SET type='AUDITORES' WHERE type='AUDITOR';
        UPDATE public.electionemailtemplate SET type='CODIGOS_VOTACION' WHERE type='VOTE_CODES';
        UPDATE public.electionemailtemplate SET type='ESTA_POR_FINALIZAR' WHERE type='ELECTION_ABOUT_TO_END';
        UPDATE public.electionemailtemplate SET type='AVISO_ELECCION' WHERE type='ELECTION_NOTICE';
        UPDATE public.electionemailtemplate SET type='RESULTADO_VOTACION' WHERE type='VOTE_RESULT';
        """
        reversed_result = pg.psql("roundtrip", "-c", reverse_sql, capture=True)
        if reversed_result.returncode != 0:
            raise RuntimeError(reversed_result.stderr or reversed_result.stdout or "No se pudo revertir el baseline sintetico")

        data20 = pg.psql("roundtrip", "-f", str(V2_0_DATA), capture=True)
        if data20.returncode != 0:
            raise RuntimeError(data20.stderr or data20.stdout or "No se pudo ejecutar v2.0_data_script.sql")

        # Para parameter/electionemailtemplate, el unico efecto de datos en v2.3 es este INSERT.
        data23 = pg.psql(
            "roundtrip",
            "-c",
            "INSERT INTO public.parameter (key, value) VALUES('AUDIT_REPORT_LINK', 'https://example.org/docs/auditReport-v2.3.pdf');",
            capture=True,
        )
        if data23.returncode != 0:
            raise RuntimeError(data23.stderr or data23.stdout or "No se pudo aplicar el efecto de datos de v2.3")

        parameter_query = "SELECT key, COALESCE(value, '') FROM public.parameter ORDER BY key"
        ref_parameters = fetch_csv_rows(pg, "reference", parameter_query)
        rt_parameters = fetch_csv_rows(pg, "roundtrip", parameter_query)

        if ref_parameters == rt_parameters:
            findings.append(Finding("parameter", "ok", "La cadena de parámetros cierra exacto contra los release files."))
        else:
            only_ref = sorted(set(ref_parameters) - set(rt_parameters))
            only_rt = sorted(set(rt_parameters) - set(ref_parameters))
            detail = [
                "La cadena de parámetros no cierra exacto contra los release files.",
                "Solo en referencia: " + "; ".join(f"{key}={value}" for key, value in only_ref),
                "Solo tras reaplicar releases: " + "; ".join(f"{key}={value}" for key, value in only_rt),
            ]
            findings.append(Finding("parameter", "error", " ".join(detail)))

        template_query = """
            SELECT
                electionemailtemplate_id::text,
                COALESCE(subjecten, ''),
                COALESCE(subjectsp, ''),
                COALESCE(subjectpt, ''),
                COALESCE(bodyen, ''),
                COALESCE(bodysp, ''),
                COALESCE(bodypt, ''),
                COALESCE(type, ''),
                COALESCE(election_id::text, '')
            FROM public.electionemailtemplate
            ORDER BY electionemailtemplate_id
        """
        ref_templates = fetch_csv_rows(pg, "reference", template_query)
        rt_templates = fetch_csv_rows(pg, "roundtrip", template_query)

        if ref_templates == rt_templates:
            findings.append(Finding("template", "ok", "electionemailtemplate_OLD cierra exacto contra los release files."))
        else:
            by_id_ref = {row[0]: row for row in ref_templates}
            by_id_rt = {row[0]: row for row in rt_templates}
            diff_ids = [template_id for template_id in by_id_ref if by_id_ref.get(template_id) != by_id_rt.get(template_id)]
            sample = ", ".join(diff_ids[:10])
            findings.append(Finding("template", "error", f"electionemailtemplate_OLD no cierra exacto. IDs con diferencias: {sample}"))

        non_global = fetch_csv_rows(
            pg,
            "reference",
            "SELECT COUNT(*)::text FROM public.electionemailtemplate WHERE election_id IS NOT NULL",
        )
        findings.append(
            Finding(
                "template",
                "ok",
                f"Filas de electionemailtemplate con election_id no nulo en la referencia: {non_global[0][0]}.",
            )
        )
    finally:
        pg.stop()

    return findings


def print_report(findings: list[Finding]) -> int:
    exit_code = 0
    print("Validacion OLD release chain")
    print()

    grouped: dict[str, list[Finding]] = {}
    for finding in findings:
        grouped.setdefault(finding.section, []).append(finding)

    for section in ("schema", "parameter", "template"):
        section_findings = grouped.get(section, [])
        if not section_findings:
            continue
        print(f"[{section}]")
        for finding in section_findings:
            marker = "OK" if finding.status == "ok" else "ERROR"
            print(f"- {marker}: {finding.message}")
            if finding.status == "error":
                exit_code = 1
        print()

    return exit_code


def main() -> int:
    try:
        findings = []
        findings.extend(validate_schema())
        findings.extend(validate_data_roundtrip())
        return print_report(findings)
    except RuntimeError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
