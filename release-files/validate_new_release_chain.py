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
REF_SCHEMA_OLD = ROOT / "ref" / "elections_schema_old.sql"
REF_TEMPLATE_OLD = ROOT / "ref" / "electionemailtemplate_OLD.sql"
REF_SCHEMA_NEW = ROOT / "ref" / "elections_schema_new.sql"
REF_TEMPLATE_NEW = ROOT / "ref" / "electionemailtemplate_NEW.sql"
REF_PARAMETER_NEW = ROOT / "ref" / "parameter_NEW.sql"
V3_0 = ROOT / "3.0" / "v3.0_script.sql"


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


def parse_schema_dump(text: str) -> tuple[dict[str, tuple[dict[str, str], dict[str, str]]], dict[str, dict[str, str]], set[str]]:
    tables: dict[str, tuple[dict[str, str], dict[str, str]]] = {}
    for match in re.finditer(r"CREATE TABLE public\.(\w+) \((.*?)\);", text, re.S):
        table_name, body = match.groups()
        columns: dict[str, str] = {}
        inline_constraints: dict[str, str] = {}
        for raw_line in body.splitlines():
            line = raw_line.strip().rstrip(",")
            if not line or line.startswith("--"):
                continue
            if line.startswith("CONSTRAINT "):
                parts = line.split(None, 2)
                inline_constraints[parts[1]] = parts[2]
                continue
            column_match = re.match(r'"?(\w+)"?\s+(.+)', line)
            if column_match:
                columns[column_match.group(1)] = column_match.group(2)
        tables[table_name] = (columns, inline_constraints)

    alter_constraints: dict[str, dict[str, str]] = {}
    for match in re.finditer(r"ALTER TABLE ONLY public\.(\w+)\n    ADD CONSTRAINT (\w+) (.*?);", text, re.S):
        table_name, constraint_name, definition = match.groups()
        alter_constraints.setdefault(table_name, {})[constraint_name] = definition.strip()

    sequences = set(re.findall(r"CREATE SEQUENCE public\.(\w+)", text))
    return tables, alter_constraints, sequences


def compare_schema(expected_text: str, actual_text: str) -> list[str]:
    expected_tables, expected_alter_constraints, expected_sequences = parse_schema_dump(expected_text)
    actual_tables, actual_alter_constraints, actual_sequences = parse_schema_dump(actual_text)

    diffs: list[str] = []

    all_tables = sorted(set(expected_tables) | set(actual_tables))
    for table_name in all_tables:
        expected = expected_tables.get(table_name)
        actual = actual_tables.get(table_name)
        if expected != actual:
            if expected is None:
                diffs.append(f"Tabla inesperada en migrado: {table_name}")
                continue
            if actual is None:
                diffs.append(f"Tabla faltante en migrado: {table_name}")
                continue

            expected_columns, expected_inline_constraints = expected
            actual_columns, actual_inline_constraints = actual

            all_columns = sorted(set(expected_columns) | set(actual_columns))
            for column_name in all_columns:
                if expected_columns.get(column_name) != actual_columns.get(column_name):
                    diffs.append(
                        f"Columna distinta en {table_name}.{column_name}: "
                        f"esperado={expected_columns.get(column_name)!r} actual={actual_columns.get(column_name)!r}"
                    )

            all_inline_constraints = sorted(set(expected_inline_constraints) | set(actual_inline_constraints))
            for constraint_name in all_inline_constraints:
                if expected_inline_constraints.get(constraint_name) != actual_inline_constraints.get(constraint_name):
                    diffs.append(
                        f"Constraint inline distinto en {table_name}.{constraint_name}: "
                        f"esperado={expected_inline_constraints.get(constraint_name)!r} actual={actual_inline_constraints.get(constraint_name)!r}"
                    )

    all_alter_tables = sorted(set(expected_alter_constraints) | set(actual_alter_constraints))
    for table_name in all_alter_tables:
        expected = expected_alter_constraints.get(table_name, {})
        actual = actual_alter_constraints.get(table_name, {})
        if expected == actual:
            continue
        all_constraint_names = sorted(set(expected) | set(actual))
        for constraint_name in all_constraint_names:
            if expected.get(constraint_name) != actual.get(constraint_name):
                diffs.append(
                    f"ALTER CONSTRAINT distinto en {table_name}.{constraint_name}: "
                    f"esperado={expected.get(constraint_name)!r} actual={actual.get(constraint_name)!r}"
                )

    if expected_sequences != actual_sequences:
        missing = sorted(expected_sequences - actual_sequences)
        unexpected = sorted(actual_sequences - expected_sequences)
        if missing:
            diffs.append("Secuencias faltantes: " + ", ".join(missing))
        if unexpected:
            diffs.append("Secuencias inesperadas: " + ", ".join(unexpected))

    return diffs


class TempPostgres:
    def __init__(self) -> None:
        self._tmp = tempfile.TemporaryDirectory(prefix="release-validation-new-")
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

    def pg_dump_schema(self, database: str) -> str:
        pg_dump = require_command("pg_dump")
        result = run_command(
            [
                pg_dump,
                "-h",
                str(self.sock),
                "-p",
                str(self.port),
                "-U",
                "postgres",
                "-d",
                database,
                "--schema-only",
                "--no-owner",
                "--no-privileges",
            ],
            env=self.env,
            capture=True,
        )
        if result.returncode != 0:
            raise RuntimeError(result.stderr or result.stdout or f"No se pudo hacer pg_dump de {database}")
        return result.stdout


def fetch_csv_rows(pg: TempPostgres, database: str, query: str) -> list[tuple[str, ...]]:
    statement = f"COPY ({query}) TO STDOUT WITH CSV"
    result = pg.psql(database, "-c", statement, capture=True)
    if result.returncode != 0:
        raise RuntimeError(result.stderr or result.stdout or f"No se pudo ejecutar query en {database}")
    return [tuple(row) for row in csv.reader(io.StringIO(result.stdout))]


def validate_roundtrip() -> list[Finding]:
    findings: list[Finding] = []
    pg = TempPostgres()
    try:
        pg.start()
        for database in ("reference", "roundtrip", "roundtrip23"):
            created = pg.psql("postgres", "-c", f"CREATE DATABASE {database};", capture=True)
            if created.returncode != 0:
                raise RuntimeError(created.stderr or created.stdout or f"No se pudo crear la base {database}")

        for sql_file in (REF_SCHEMA_NEW, REF_PARAMETER_NEW, REF_TEMPLATE_NEW):
            loaded = pg.psql("reference", "-f", str(sql_file), capture=True)
            if loaded.returncode != 0:
                raise RuntimeError(loaded.stderr or loaded.stdout or f"No se pudo cargar {sql_file}")

        for database in ("roundtrip", "roundtrip23"):
            for sql_file in (REF_SCHEMA_OLD, REF_TEMPLATE_OLD):
                loaded = pg.psql(database, "-f", str(sql_file), capture=True)
                if loaded.returncode != 0:
                    raise RuntimeError(loaded.stderr or loaded.stdout or f"No se pudo cargar {sql_file}")

            if database == "roundtrip23":
                restored_23_column = pg.psql(
                    database,
                    "-c",
                    "ALTER TABLE public.commissioner ADD COLUMN resulttoken character varying(1000);",
                    capture=True,
                )
                if restored_23_column.returncode != 0:
                    raise RuntimeError(restored_23_column.stderr or restored_23_column.stdout or "No se pudo simular el schema v2.3")

            loaded = pg.psql(database, "-f", str(V3_0), capture=True)
            if loaded.returncode != 0:
                raise RuntimeError(loaded.stderr or loaded.stdout or f"No se pudo cargar {V3_0}")

        schema_diffs = compare_schema(pg.pg_dump_schema("reference"), pg.pg_dump_schema("roundtrip"))
        if not schema_diffs:
            findings.append(Finding("schema", "ok", "elections_schema_new cierra exacto desde elections_schema_old + v3.0 consolidado."))
        else:
            findings.append(Finding("schema", "error", "Diferencias de esquema: " + " | ".join(schema_diffs[:12])))

        schema_diffs_23 = compare_schema(pg.pg_dump_schema("reference"), pg.pg_dump_schema("roundtrip23"))
        if not schema_diffs_23:
            findings.append(Finding("schema", "ok", "elections_schema_new cierra exacto desde v2.3 simulado + v3.0 consolidado."))
        else:
            findings.append(Finding("schema", "error", "Diferencias de esquema desde v2.3: " + " | ".join(schema_diffs_23[:12])))

        parameter_query = "SELECT key, COALESCE(value, '') FROM public.parameter ORDER BY key"
        ref_parameters = fetch_csv_rows(pg, "reference", parameter_query)
        rt_parameters = fetch_csv_rows(pg, "roundtrip", parameter_query)
        findings.append(
            Finding(
                "parameter",
                "ok" if ref_parameters == rt_parameters else "error",
                "parameter_NEW cierra exacto contra la migracion."
                if ref_parameters == rt_parameters
                else "parameter_NEW no cierra exacto contra la migracion.",
            )
        )

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
        findings.append(
            Finding(
                "template",
                "ok" if ref_templates == rt_templates else "error",
                "electionemailtemplate_NEW cierra exacto contra la migracion."
                if ref_templates == rt_templates
                else "electionemailtemplate_NEW no cierra exacto contra la migracion.",
            )
        )

        uservoter_unique_index = fetch_csv_rows(
            pg,
            "roundtrip",
            """
            SELECT indexname
            FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename = 'uservoter'
              AND indexname = 'uq_uservoter_election_orgid_norm'
            """,
        )
        findings.append(
            Finding(
                "schema",
                "ok" if uservoter_unique_index == [("uq_uservoter_election_orgid_norm",)] else "error",
                "Existe el índice único de ORGID normalizado por elección en uservoter."
                if uservoter_unique_index == [("uq_uservoter_election_orgid_norm",)]
                else f"No se encontró el índice único esperado de uservoter.orgid: {uservoter_unique_index}",
            )
        )
    finally:
        pg.stop()

    return findings


def validate_fixture_migration() -> list[Finding]:
    findings: list[Finding] = []
    pg = TempPostgres()
    try:
        pg.start()
        created = pg.psql("postgres", "-c", "CREATE DATABASE fixture;", capture=True)
        if created.returncode != 0:
            raise RuntimeError(created.stderr or created.stdout or "No se pudo crear la base fixture")

        for sql_file in (REF_SCHEMA_OLD, REF_TEMPLATE_OLD):
            loaded = pg.psql("fixture", "-f", str(sql_file), capture=True)
            if loaded.returncode != 0:
                raise RuntimeError(loaded.stderr or loaded.stdout or f"No se pudo cargar {sql_file}")

        seed_sql = """
        INSERT INTO public.election (
            election_id, auditorsset, randomordercandidates, candidatesset, category,
            descriptionspanish, descriptionenglish, descriptionportuguese, diffutc,
            creationdate, enddate, startdate, auditorlinkavailable, resultlinkavailable,
            votinglinkavailable, migration_id, linkspanish, linkenglish, linkportuguese,
            maxcandidates, migrated, electorsset, defaultsender, revisionrequest, onlysp,
            titlespanish, titleenglish, titleportuguese, resulttoken, closed, closeddate
        ) VALUES
        (
            100, true, true, true, 'STATUTORY',
            'Descripcion ES', 'Description EN', 'Descricao PT', 3,
            '2026-01-01 12:00:00', '2026-02-15 18:00:00', '2026-02-10 12:00:00', false, false,
            true, NULL, 'https://example.com/es/100', 'https://example.com/en/100', 'https://example.com/pt/100',
            4, false, true, 'sender@example.com', false, true,
            'Eleccion 100', 'Election 100', 'Eleicao 100', 'result-100', false, NULL
        ),
        (
            101, false, true, false, 'MODERATORS',
            'Descripcion ES 101', 'Description EN 101', 'Descricao PT 101', 3,
            '2026-03-01 08:00:00', '2026-04-15 18:00:00', '2026-04-10 09:00:00', false, false,
            true, NULL, 'https://example.com/es/101', 'https://example.com/en/101', 'https://example.com/pt/101',
            2, false, true, 'sender@example.com', false, true,
            'Eleccion 101', 'Election 101', 'Eleicao 101', 'result-101', false, NULL
        );

        INSERT INTO public.candidate (
            candidate_id, biospanish, bioenglish, bioportuguese, pictureinfo, pictureextension,
            migration_id, linkspanish, linkenglish, linkportuguese, name, picturename,
            candidateorder, onlysp, election_id, mail
        ) VALUES
        (
            200, 'Bio ES', 'Bio EN', 'Bio PT', decode('00', 'hex'), 'jpg',
            NULL, 'https://candidate/es', 'https://candidate/en', 'https://candidate/pt',
            'Candidate 200', 'candidate200.jpg', 1, false, 100, 'candidate200@example.com'
        ),
        (
            201, 'Bio ES 201', 'Bio EN 201', 'Bio PT 201', decode('00', 'hex'), 'jpg',
            NULL, 'https://candidate201/es', 'https://candidate201/en', 'https://candidate201/pt',
            'Candidate 201', 'candidate201.jpg', 1, false, 101, 'candidate201@example.com'
        );

        INSERT INTO public.auditor (
            auditor_id, commissioner, agreedconformity, revisionavailable, migration_id,
            mail, name, resulttoken, election_id
        ) VALUES
        (
            300, false, false, false, NULL,
            'auditor300@example.com', 'Auditor 300', 'auditor-token-300', 100
        ),
        (
            301, false, false, false, NULL,
            'auditor301@example.com', 'Auditor 301', 'auditor-token-301', 101
        );

        INSERT INTO public.electionemailtemplate (
            electionemailtemplate_id, subjecten, subjectsp, subjectpt,
            bodyen, bodysp, bodypt, "type", election_id
        ) VALUES (
            2169, 'Fixture election EN', 'Fixture election ES', 'Fixture election PT',
            'Fixture body EN', 'Fixture body ES', 'Fixture body PT', 'ELECTION_NOTICE', 100
        );

        INSERT INTO public.uservoter (
            uservoter_id, voteamount, votedate, migration_id, language, mail, name,
            orgid, country, votetoken, voted, election_id, version
        ) VALUES
        (
            400, 2, NULL, NULL, 'SP', 'voter400@example.com', 'Voter 400',
            NULL, 'UY', 'vote-token-400', false, 100, 0
        ),
        (
            401, 1, NULL, NULL, 'EN', 'voter401@example.com', 'Voter 401',
            '   ', 'BR', 'vote-token-401', false, 101, 0
        );
        """
        seeded = pg.psql("fixture", "-c", seed_sql, capture=True)
        if seeded.returncode != 0:
            raise RuntimeError(seeded.stderr or seeded.stdout or "No se pudo sembrar la fixture legacy")

        for sql_file in (V3_0,):
            loaded = pg.psql("fixture", "-f", str(sql_file), capture=True)
            if loaded.returncode != 0:
                raise RuntimeError(loaded.stderr or loaded.stdout or f"No se pudo cargar {sql_file} sobre fixture")

        election_rows = fetch_csv_rows(
            pg,
            "fixture",
            """
            SELECT
                election_id::text,
                COALESCE(election_type, ''),
                managevotersmanual::text,
                manageorganizationsmanual::text,
                CASE WHEN public_election_token IS NULL OR public_election_token = '' THEN 'MISSING' ELSE 'OK' END
            FROM public.election
            ORDER BY election_id
            """,
        )
        expected_election_rows = [
            ("100", "BOARD", "true", "true", "OK"),
            ("101", "MODERATORS", "true", "true", "OK"),
        ]
        findings.append(
            Finding(
                "fixture",
                "ok" if election_rows == expected_election_rows else "error",
                "Mapeo final de election_type y flags manuales sobre fixture legacy."
                if election_rows == expected_election_rows
                else f"Mapeo inesperado en fixture: {election_rows}",
            )
        )

        voter_orgid_rows = fetch_csv_rows(
            pg,
            "fixture",
            """
            SELECT
                uservoter_id::text,
                COALESCE(orgid, '')
            FROM public.uservoter
            WHERE uservoter_id IN (400, 401)
            ORDER BY uservoter_id
            """,
        )
        findings.append(
            Finding(
                "fixture",
                "ok" if voter_orgid_rows == [("400", "voter400@example.com"), ("401", "voter401@example.com")] else "error",
                "Los uservoter legacy sin ORGID recuperan el mail como ORGID."
                if voter_orgid_rows == [("400", "voter400@example.com"), ("401", "voter401@example.com")]
                else f"Backfill inesperado de uservoter.orgid: {voter_orgid_rows}",
            )
        )

        calendar_counts = fetch_csv_rows(
            pg,
            "fixture",
            "SELECT election_id::text, COUNT(*)::text FROM public.electioncalendar GROUP BY election_id ORDER BY election_id",
        )
        findings.append(
            Finding(
                "fixture",
                "ok" if calendar_counts == [("100", "23"), ("101", "23")] else "error",
                "Cada eleccion legacy recibe los 23 electioncalendar."
                if calendar_counts == [("100", "23"), ("101", "23")]
                else f"Cantidad inesperada de calendars: {calendar_counts}",
            )
        )

        task_counts = fetch_csv_rows(
            pg,
            "fixture",
            "SELECT election_id::text, COUNT(*)::text FROM public.electiontask GROUP BY election_id ORDER BY election_id",
        )
        findings.append(
            Finding(
                "fixture",
                "ok" if task_counts == [("100", "9"), ("101", "7")] else "error",
                "Cada eleccion legacy recibe el set de electiontask esperado."
                if task_counts == [("100", "9"), ("101", "7")]
                else f"Cantidad inesperada de tasks: {task_counts}",
            )
        )

        date_rows = fetch_csv_rows(
            pg,
            "fixture",
            """
            SELECT
                election_id::text,
                calendarkey,
                to_char(startdate, 'YYYY-MM-DD HH24:MI:SS'),
                to_char(enddate, 'YYYY-MM-DD HH24:MI:SS')
            FROM public.electioncalendar
            WHERE (election_id = 100 AND calendarkey IN ('N_16_PERIODO_VOTING', 'N_2_PERIODO_CALL_FOR_CANDIDATES', 'N_23_PERIODO_PADRON_MILACNIC_SYNC'))
               OR (election_id = 101 AND calendarkey IN ('N_16_PERIODO_VOTING', 'N_2_PERIODO_CALL_FOR_CANDIDATES', 'N_23_PERIODO_PADRON_MILACNIC_SYNC'))
            ORDER BY election_id, calendarkey
            """,
        )
        expected_date_rows = [
            ("100", "N_16_PERIODO_VOTING", "2026-02-10 12:00:00", "2026-02-15 18:00:00"),
            ("100", "N_23_PERIODO_PADRON_MILACNIC_SYNC", "2026-02-10 12:00:00", "2026-02-10 12:00:00"),
            ("100", "N_2_PERIODO_CALL_FOR_CANDIDATES", "2026-02-10 12:00:00", "2026-02-10 12:00:00"),
            ("101", "N_16_PERIODO_VOTING", "2026-04-10 09:00:00", "2026-04-15 18:00:00"),
            ("101", "N_23_PERIODO_PADRON_MILACNIC_SYNC", "2026-04-10 09:00:00", "2026-04-10 09:00:00"),
            ("101", "N_2_PERIODO_CALL_FOR_CANDIDATES", "2026-04-10 09:00:00", "2026-04-10 09:00:00"),
        ]
        findings.append(
            Finding(
                "fixture",
                "ok" if date_rows == expected_date_rows else "error",
                "Las fechas legacy se migran a N_16 y a los calendars preset con fecha puntual."
                if date_rows == expected_date_rows
                else f"Fechas inesperadas en calendars: {date_rows}",
            )
        )

        candidate_rows = fetch_csv_rows(
            pg,
            "fixture",
            """
            SELECT
                candidate_id::text,
                COALESCE(candidate_type, ''),
                COALESCE(status, ''),
                COALESCE(reminder_frequency, '')
            FROM public.candidate
            ORDER BY candidate_id
            """,
        )
        findings.append(
            Finding(
                "fixture",
                "ok" if candidate_rows == [("200", "NORMAL", "CONFIRMED_AND_PUBLISHED", "DISABLED"), ("201", "NORMAL", "CONFIRMED_AND_PUBLISHED", "DISABLED")] else "error",
                "Los candidatos legacy reciben candidate_type/status/reminder_frequency esperados."
                if candidate_rows == [("200", "NORMAL", "CONFIRMED_AND_PUBLISHED", "DISABLED"), ("201", "NORMAL", "CONFIRMED_AND_PUBLISHED", "DISABLED")]
                else f"Mapeo inesperado en candidate: {candidate_rows}",
            )
        )

        auditor_rows = fetch_csv_rows(
            pg,
            "fixture",
            """
            SELECT
                auditor_id::text,
                COALESCE(reminder_frequency, '')
            FROM public.auditor
            ORDER BY auditor_id
            """,
        )
        findings.append(
            Finding(
                "fixture",
                "ok" if auditor_rows == [("300", "DISABLED"), ("301", "DISABLED")] else "error",
                "Los auditores legacy reciben reminder_frequency DISABLED."
                if auditor_rows == [("300", "DISABLED"), ("301", "DISABLED")]
                else f"Mapeo inesperado en auditor: {auditor_rows}",
            )
        )

        election_template_rows = fetch_csv_rows(
            pg,
            "fixture",
            """
            SELECT
                election_id::text,
                COALESCE(type, ''),
                COALESCE(subjecten, ''),
                COALESCE(subjectsp, ''),
                COALESCE(subjectpt, ''),
                CASE WHEN electionemailtemplate_id > 4288 THEN 'true' ELSE 'false' END
            FROM public.electionemailtemplate
            WHERE election_id IS NOT NULL
            ORDER BY election_id, type
            """,
        )
        findings.append(
            Finding(
                "fixture",
                "ok" if election_template_rows == [("100", "ELECTION_NOTICE", "Fixture election EN", "Fixture election ES", "Fixture election PT", "true")] else "error",
                "Los templates por eleccion legacy se preservan aunque sus IDs choquen con la nueva base."
                if election_template_rows == [("100", "ELECTION_NOTICE", "Fixture election EN", "Fixture election ES", "Fixture election PT", "true")]
                else f"Templates por eleccion inesperados tras migracion: {election_template_rows}",
            )
        )
    finally:
        pg.stop()

    return findings


def print_report(findings: list[Finding]) -> int:
    exit_code = 0
    print("Validacion NEW release chain")
    print()

    grouped: dict[str, list[Finding]] = {}
    for finding in findings:
        grouped.setdefault(finding.section, []).append(finding)

    for section in ("schema", "parameter", "template", "fixture"):
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
        findings.extend(validate_roundtrip())
        findings.extend(validate_fixture_migration())
        return print_report(findings)
    except RuntimeError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
