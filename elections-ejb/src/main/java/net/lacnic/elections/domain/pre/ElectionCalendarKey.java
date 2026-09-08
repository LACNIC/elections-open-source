package net.lacnic.elections.domain.pre;

public enum ElectionCalendarKey {

	N_23_PERIODO_PADRON_MILACNIC_SYNC(
			"Ventana de sincronización del padrón con MiLACNIC. Operativo: durante este período se sincronizan organizaciones desde el WS de MiLACNIC cada 2 horas.",
			"Janela de sincronização do padrón com MiLACNIC. Operacional: durante este período as organizações são sincronizadas do WS de MiLACNIC a cada 2 horas.",
			"Census synchronization window with MiLACNIC. Operational: during this period, organizations are synchronized from the MiLACNIC WS every 2 hours."),
	N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED(
			"Publicación de la elección. Operativo: desde esta fecha se habilita el acceso a la página pública de la elección y se muestra el calendario público con los hitos configurados como publicables.",
			"Publicação da eleição. Operacional: a partir desta data, o acesso à página pública da eleição é habilitado e o calendário público é exibido com os marcos configurados como publicáveis.",
			"Election publication. Operational: from this date, the public election page is available and the public calendar is shown with milestones configured as public."),
	N_2_PERIODO_CALL_FOR_CANDIDATES(
			"Inicio del llamado a candidatos. Operativo: Duración: 2 semanas.",
			"Início do chamado a candidatos. Operacional: Duração: 2 semanas.",
			"Start of the call for candidates. Operational: Duration: 2 weeks."),
	N_3_SINGLE_PADRON_CLOSED(
			"Cierre del padrón electoral (socios habilitados). Operativo: después del inicio del llamado y antes de la publicación.",
			"Fechamento do padrón eleitoral (associados habilitados). Operacional: após o início do chamado e antes da publicação.",
			"Close of the electoral roll (eligible members). Operational: after the start of the call and before publication."),
	N_4_SINGLE_PADRON_PUBLISHED(
			"Publicación del padrón electoral. Estatutarias: a más tardar 30 días luego del inicio proceso electoral (nuevo requisito Asamblea 2022).",
			"Publicação do padrón eleitoral. Estatutário: no máximo 30 dias após o início do processo eleitoral (novo requisito da Assembleia 2022).",
			"Publication of the electoral roll. Statutory: no later than 30 days after the start of the electoral process (new 2022 Assembly requirement)."),
	N_5_PERIODO_PADRON_CLAIMS(
			"Período de posibles reclamos, ajustes o aclaraciones sobre el padrón electoral. Estatutarias: hasta 15 días antes del inicio de la votación (nuevo requisito Asamblea 2022).",
			"Período de possíveis reclamações, ajustes ou esclarecimentos sobre o padrón eleitoral. Estatuto: até 15 dias antes do início da votação (novo requisito da Assembleia 2022).",
			"Period for possible claims, adjustments, or clarifications on the electoral roll. Statutory: up to 15 days before the start of voting (new 2022 Assembly requirement)."),
	N_6_PERIODO_ADDITIONAL_EVALUATION(
			"Semana adicional para rendir evaluación. Operativo: una semana adicional luego del cierre del llamado a candidatos.",
			"Semana adicional para realizar a avaliação. Operacional: uma semana adicional após o fechamento do chamado a candidatos.",
			"Additional week to take the evaluation. Operational: one additional week after the close of the call for candidates."),
	N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION(
			"Fin del período de observaciones sobre los candidatos (fecha interna para la C.E. y el staff, no se publica). Operativo: 3 días hábiles posteriores al cierre del llamado.",
			"Fim do período de observações sobre os candidatos (data interna para a CE e a equipe, não se publica). Operacional: 3 dias úteis após o fechamento do chamado.",
			"End of the period for observations on candidates (internal date for the EC and staff, not published). Operational: 3 business days after the close of the call."),
	N_8_PERIODO_EVALUATIONS_VALIDATION(
			"Fin del período de observaciones sobre las evaluaciones (fecha interna para la CE y el staff, no se publica). Operativo: 5 días hábiles posteriores al cierre del periodo de verificación de formularios.",
			"Fim do período de observações sobre as avaliações (data interna para a CE e a equipe, não se publica). Operacional: 5 dias úteis após o fechamento do período de verificação de formulários.",
			"End of the period for observations on evaluations (internal date for the EC and staff, not published). Operational: 5 business days after the close of the candidate form verification period."),
	N_9_PERIODO_CANDIDATE_CLAIMS(
			"Recepción de reclamos de candidatos. Operativo: a partir del último día de evaluación por parte de la CE. Envío de mail de validación o no de candidatura (3 días hábiles).",
			"Recepção de reclamações de candidatos. Operacional: a partir do último dia de avaliação pela CE. Envio de e-mail de validação ou não da candidatura (3 dias úteis).",
			"Reception of candidate claims. Operational: starting from the last day of evaluation by the EC. Send validation/non-validation email of candidacy (3 business days)."),
	N_10_PERIODO_CANDIDATE_CLAIMS_RESOLUTION(
			"Límite para resolver reclamos de candidatos (fecha interna para la CE y el staff, no se publica). Operativo: 3 días hábiles luego del fin de recepción de reclamos de candidatos.",
			"Prazo para resolver reclamações de candidatos (data interna para a CE e a equipe, não se publica). Operacional: 3 dias úteis após o fim da recepção de reclamações de candidatos.",
			"Deadline to resolve candidate claims (internal date for the EC and staff, not published). Operational: 3 business days after the end of candidate claim reception."),
	N_11_SINGLE_CANDIDATES_PUBLISHED(
			"Publicación de los candidatos. Operativo: post cierre llamado cierre reclamos candidatos por evaluación (para traducciones y armado de material/bios candidatos).",
			"Publicação dos candidatos. Operacional: após o fechamento do chamado e o encerramento das reclamações por avaliação (para traduções e preparação de materiais/bios dos candidatos).",
			"Publication of candidates. Operational: after closing the call and closing evaluation claims (for translations and preparation of candidate materials/bios)."),
	N_12_PERIODO_CANDIDATE_QUESTIONS(
			"Envío de preguntas a los candidatos. Operativo: 3 días hábiles posteriores de la publicación de los candidatos.",
			"Envio de perguntas aos candidatos. Operacional: 3 dias úteis após a publicação dos candidatos.",
			"Sending questions to candidates. Operational: 3 business days after the publication of candidates."),
	N_13_PERIODO_PADRON_CLAIMS_RESOLUTION(
			"Fin del período de resoluciones sobre reclamos, ajustes o aclaraciones del padrón electoral. Estatutarias: 5 días posteriores al último día de reclamos sobre el padrón (nuevo requisito Asamblea 2022).",
			"Fim do período de resoluções sobre reclamações, ajustes ou esclarecimentos do padrón eleitoral. Estatuto: 5 dias após o último dia de reclamações sobre o padrón (novo requisito da Assembleia 2022).",
			"End of the period to resolve claims, adjustments, or clarifications of the electoral roll. Statutory: 5 days after the last day of roll claims (new 2022 Assembly requirement)."),
	N_14_SINGLE_PADRON_UPDATED(
			"STAFF: se agregan las organizaciones que abonaron la membresía hasta la fecha. Operativo: +/- 3 días antes del inicio de la votación online.",
			"Staff: são adicionadas as organizações que pagaram a membresia até a data. Operacional: +/- 3 dias antes do início da votação online.",
			"Staff: organizations that have paid the membership are added up to that date. Operational: +/- 3 days before the start of online voting."),
	N_15_PERIODO_CANDIDATE_CLAIMS_BY_CE(
			"Fin del período de respuestas a reclamos de asociados sobre candidatos (respuestas por parte de la C.E.). Operativo: 2 días hábiles posteriores al último día de reclamos sobre los candidatos.",
			"Fim do período de respostas às reclamações de associados sobre candidatos (respostas pela CE). Operacional: 2 dias úteis após o último dia de reclamações sobre os candidatos.",
			"End of the period for responses to member claims about candidates (responses by the EC). Operational: 2 business days after the last day of candidate claims."),
	N_16_PERIODO_VOTING(
			"Inicio de la votación online. Estatutaria y Moderadores: 1 semana (5 días hábiles) / ASO/RC online: 3 días (hábiles).",
			"Início da votação online. Estatutária e Moderadores: 1 semana (5 dias úteis) / ASO/RC online: 3 dias (úteis).",
			"Start of online voting. Statutory and Moderators: 1 week (5 business days) / ASO/RC online: 3 (business) days."),
	N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED(
			"Publicación de los resultados provisorios sujeto a auditorías de la C.E. y por parte de los votantes. Operativo: Minutos después del cierre de la votación.",
			"Publicação dos resultados provisórios sujeitos a auditorias da CE e pelos votantes. Operacional: Minutos após o encerramento da votação.",
			"Publication of provisional results subject to audits by the EC and voters. Operational: Minutes after the close of voting."),
	N_18_PERIODO_CE_AUDIT(
			"Período de auditoría por parte de la Comisión Electoral. Operativo: 2 días y medio hábiles (a partir del momento que cierra la votación).",
			"Período de auditoria pela Comissão Eleitoral. Operacional: 2 dias e meio úteis (a partir do momento em que a votação encerra).",
			"Audit period by the Electoral Commission. Operational: 2.5 business days (from the moment voting closes)."),
	N_19_PERIODO_VOTER_AUDIT(
			"Período de auditoría por parte de los votantes y recepción de posibles reclamos sobre la votación. Operativo: 2 días hábiles (posteriores al último día de la auditoría de la C.E.).",
			"Período de auditoria pelos votantes e recepção de possíveis reclamações sobre a votação. Operacional: 2 dias úteis (posteriores ao último dia da auditoria da CE).",
			"Audit period by voters and reception of possible claims about voting. Operational: 2 business days (after the last day of the EC audit)."),
	N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED(
			"Publicación del resultado oficial si no hubo reclamos sobre la votación (con publicación de la carta certificadora del resultado firmada por la C.E., para elecciones estatutarias). Operativo: Una vez finalizado el período de posibles reclamos.",
			"Publicação do resultado oficial se não houve reclamações sobre a votação (com publicação da carta certificadora do resultado assinada pela CE, para eleições estatutárias). Operacional: Uma vez finalizado o período de possíveis reclamações.",
			"Publication of the official result if there were no claims about voting (with publication of the certification letter signed by the EC, for statutory elections). Operational: Once the period for possible claims is finished."),
	N_21_PERIODO_VOTER_CLAIMS_RESPONSE(
			"Fin del período de respuestas ante posibles reclamos de votantes (respuestas por parte de la C.E.). Operativo: 3 días hábiles (posteriores al fin del período de posibles reclamos).",
			"Fim do período de respostas a possíveis reclamações de votantes (respostas pela CE). Operacional: 3 dias úteis (posteriores ao fim do período de possíveis reclamações).",
			"End of the period for responses to possible voter claims (responses by the EC). Operational: 3 business days (after the end of the possible claims period)."),
	N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED(
			"Publicación del resultado oficial si hubo reclamos sobre la votación. Operativo: El día hábil siguiente al fin del período de respuestas de la C.E. a los posibles reclamos.",
			"Publicação do resultado oficial se houve reclamações sobre a votação. Operacional: O dia útil seguinte ao fim do período de respostas da CE às possíveis reclamações.",
			"Publication of the official result if there were claims about voting. Operational: The business day following the end of the EC response period to possible claims.");

	private final String descriptionES;
	private final String descriptionPT;
	private final String descriptionEN;

	ElectionCalendarKey(String descriptionES, String descriptionPT, String descriptionEN) {
		this.descriptionES = descriptionES;
		this.descriptionPT = descriptionPT;
		this.descriptionEN = descriptionEN;
	}

	public String getDescriptionES() {
		return descriptionES;
	}

	public String getDescriptionPT() {
		return descriptionPT;
	}

	public String getDescriptionEN() {
		return descriptionEN;
	}
}
