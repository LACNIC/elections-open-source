DELETE FROM public.electionemailtemplate WHERE election_id IS NOT NULL;
UPDATE public.electionemailtemplate set type='NEW' where type='NUEVO';
UPDATE public.electionemailtemplate set type='SIGNATURE' where type='FIRMA';
UPDATE public.electionemailtemplate set type='ELECTION_START' where type='COMIENZO_ELECCION';
UPDATE public.electionemailtemplate set type='AUDITOR_AGREEMENT' where type='AUDITOR_CONFORME';
UPDATE public.electionemailtemplate set type='AUDITOR' where type='AUDITORES';
UPDATE public.electionemailtemplate set type='VOTE_CODES' where type='CODIGOS_VOTACION';
UPDATE public.electionemailtemplate set type='ELECTION_ABOUT_TO_END' where type='ESTA_POR_FINALIZAR';
UPDATE public.electionemailtemplate set type='ELECTION_NOTICE' where type='AVISO_ELECCION';
UPDATE public.electionemailtemplate set type='VOTE_RESULT' where type='RESULTADO_VOTACION';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] $election.titleEnglish',
subjectpt='[Eleições] $election.titlePortuguese',
bodyen='Dear,'||E'\n\n'||'The auditor $auditor.name, has enabled revision for election: $election.titleEnglish',
bodysp='Estimados,'||E'\n\n'||'El auditor $auditor.name, ha habilitado la revisión de la elección: $election.titleSpanish',
bodypt='Prezados,'||E'\n\n'||'No(a) auditor(a) $auditor.name, habilitou a revisão da eleição: $election.titlePortuguese',
subjectsp='[Elecciones] $election.titleSpanish'
WHERE election_id is null
AND type='AUDITOR_REVISION';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] $election.titleEnglish',
subjectpt='[Eleições] $election.titlePortuguese',
bodyen='Dear $user.name,'||E'\n\n'||'From this moment on your link is enabled to participate in '||E'\n\n'||'$election.titleEnglish.'||E'\n\n'||'You can now access it below and vote:'||E'\n\n'||'$user.voteLink'||E'\n\n\n',
bodysp='Estimado/a $user.name,'||E'\n\n'||'En este momento está activo su enlace para participar en '||E'\n\n'||'$election.titleSpanish'||E'\n\n'||'A partir de ahora usted podrá acceder al siguiente enlace y votar'||E'\n\n'||'$user.voteLink'||E'\n\n\n',
bodypt='Prezado(a) $user.name,'||E'\n\n'||'A partir deste momento está ativado o seu link para participar na '||E'\n\n'||'$election.titlePortuguese'||E'\n\n'||'Você já pode acessá-lo abaixo e votar:'||E'\n\n'||'$user.voteLink'||E'\n\n\n',
subjectsp='[Elecciones] $election.titleSpanish'
WHERE election_id is null
AND type='ELECTION_START';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] $election.titleEnglish',
subjectpt='[Eleições] $election.titlePortuguese',
bodyen='Dear,'||E'\n\n'||'The auditor $auditor.name, is in agreement with the $election.titleEnglish'||E'\n\n',
bodysp='Estimados,'||E'\n\n'||'El auditor $auditor.name, ha indicado su conformidad con $election.titleSpanish'||E'\n\n',
bodypt='Prezados,'||E'\n\n'||'O(a) auditor(a) $auditor.name, está de acordo com a $election.titlePortuguese'||E'\n\n',
subjectsp='[Elecciones] $election.titleSpanish'
WHERE election_id is null
AND type='AUDITOR_AGREEMENT';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] Audit',
subjectpt='[Eleições] Auditoria',
bodyen='Dear $auditor.name,'||E'\n\n'||'To view the results and audit the votes of'||E'\n\n'||'$election.titleEnglish'||E'\n\n'||'Please go to the following link:'||E'\n\n'||'$auditor.resultLink'||E'\n\n',
bodysp='Estimado/a $auditor.name,'||E'\n\n'||'Para visualizar los resultados y auditar los votos de'||E'\n\n'||'$election.titleSpanish'||E'\n\n'||'Por favor, ingresar en el siguiente enlace'||E'\n\n'||'$auditor.resultLink'||E'\n\n',
bodypt='Prezado(a) $auditor.name,'||E'\n\n'||'Para visualizar os resultados e auditar os votos da'||E'\n\n'||'$election.titlePortuguese'||E'\n\n'||'Por favor, ingresse ao seguinte link:'||E'\n\n'||'$auditor.resultLink'||E'\n\n',
subjectsp='[Elecciones] Auditoría'
WHERE election_id is null
AND type='AUDITOR';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] Verification Codes',
subjectpt='[Eleições] Códigos de verificação',
bodyen='Dear $user.name,'||E'\n\n'||'Thank you for your participation in the $election.titleEnglish'||E'\n\n'||'We received your vote correctly and we are sending the verification codes below:'||E'\n\n'||'CODE/CANDIDATE'||E'\n\n'||'----------------------------------------'||E'\n\n'||'$user.codesSummary'||E'\n\n'||'From $election.endDateString forward, you will be able to access the following link and see the results of the election:'||E'\n\n'||'$election.resultLink'||E'\n\n',
bodysp='Estimado/a $user.name,'||E'\n\n'||'Gracias por su participación en la $election.titleSpanish'||E'\n\n'||'Hemos recibido su voto correctamente y a continuación le enviamos los codigos de verificación:'||E'\n\n'||'CODIGO/CANDIDATO'||E'\n\n'||'----------------------------------------'||E'\n\n'||'$user.codesSummary'||E'\n\n'||'A partir del día $election.endDateString podrá acceder al siguiente enlace y ver los resultados de la elección:'||E'\n\n'||'$election.resultLink'||E'\n\n',
bodypt='Prezado(a)  $user.name,'||E'\n\n'||'Agradecemos a sua participação na $election.titlePortuguese.'||E'\n\n'||'Recebemos corretamente o seu voto e a continuação lhe enviamos os códigos de verificação:'||E'\n\n'||'CÓDIGO/CANDIDATO'||E'\n\n'||'----------------------------------------'||E'\n\n'||'$user.codesSummary'||E'\n\n'||'A partir do dia $election.endDateString você poderá acessar ao seguinte link e ver os resultados da eleição:'||E'\n\n'||'$election.resultLink'||E'\n\n',
subjectsp='[Elecciones] Códigos de verificación'
WHERE election_id is null
AND type='VOTE_CODES';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] $election.titleEnglish',
subjectpt='[Eleições] $election.titlePortuguese',
bodyen='Dear $user.name,'||E'\n\n'||'There is no much more time left to participate in this $election.titleEnglish'||E'\n\n'||'Remember that you can access the following link and vote:'||E'\n\n'||'$user.voteLink'||E'\n\n'||'The deadline to vote is $election.endDateString'||E'\n\n',
bodysp='Estimado/a $user.name,'||E'\n\n'||'Queda poco tiempo para paticipar en la $election.titleSpanish'||E'\n\n'||'Recuerde que usted puede acceder al siguiente enlace y votar:'||E'\n\n'||'$user.voteLink'||E'\n\n'||'La fecha limite para votar es $election.endDateString'||E'\n\n',
bodypt='Prezado(a) $user.name,'||E'\n\n'||'Resta pouco tempo para participar na $election.titlePortuguese'||E'\n\n'||'Lembre-se que vocÍ pode acessar ao seguinte link e votar:'||E'\n\n'||'$user.voteLink'||E'\n\n'||'A data limite para votar è $election.endDateString'||E'\n\n',
subjectsp='[Elecciones] $election.titleSpanish'
WHERE election_id is null
AND type='ELECTION_ABOUT_TO_END';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] $election.titleEnglish',
subjectpt='[Eleições] $election.titlePortuguese',
bodyen='Dear $user.name,'||E'\n\n'||'The following $election.startDateString the $election.titleEnglish will begin.'||E'\n\n'||'From that moment on you will be able to access the link below and vote:'||E'\n\n'||'$user.voteLink'||E'\n\n',
bodysp='Estimado/a $user.name,'||E'\n\n'||'El próximo día $election.startDateString comenzará la $election.titleSpanish.'||E'\n\n'||'A partir de ese momento usted podrá acceder al siguiente enlace y votar:'||E'\n\n'||'$user.voteLink'||E'\n\n',
bodypt='Prezado(a) $user.name,'||E'\n\n'||'No próximo dia $election.startDateString começará a $election.titlePortuguese.'||E'\n\n'||'A partir desse momento você poderá acessar ao seguinte link e votar:'||E'\n\n'||'$user.voteLink'||E'\n\n',
subjectsp='[Elecciones] $election.titleSpanish'
WHERE election_id is null
AND type='ELECTION_NOTICE';

UPDATE public.electionemailtemplate
SET subjecten='[Elections] Results',
subjectpt='[Eleições] Resultados',
bodyen='Dear $user.name,'||E'\n\n'||'To view the results and verify your votes from $election.titleEnglish, please go to the following link:'||E'\n\n'||'$election.resultLink'||E'\n\n',
bodysp='Estimado/a $user.name,'||E'\n\n'||'Para visualizar los resultados y verificar sus votos de la $election.titleSpanish, por favor ingrese en el siguiente enlace:'||E'\n\n'||'$election.resultLink'||E'\n\n',
bodypt='Prezado(a) $user.name,'||E'\n\n'||'Para visualizar os resultados e verificar seus votos da $election.titlePortuguese, por favor ingresse ao seguinte link:'||E'\n\n'||'$election.resultLink'||E'\n\n',
subjectsp='[Elecciones] Resultados'
WHERE election_id is null
AND type='VOTE_RESULT';

UPDATE public.parameter set key='DEFAULT_SENDER' WHERE key='REMITENTE_ESTANDAR';
UPDATE public.parameter set key='WS_AUTHORIZED_IPS' WHERE key='WS_IPS_HABILITADAS';
UPDATE public.parameter set key='EMAIL_PASSWORD' WHERE key='EMAIL_CLAVE';
UPDATE public.parameter set key='EMAIL_USER' WHERE key='EMAIL_USUARIO';

UPDATE public.election set category='STATUTORY' WHERE category='ESTATUTARIA';
UPDATE public.election set category='MODERATORS' WHERE category='MODERADORES';
UPDATE public.election set category='OTHER' WHERE category='OTRA';

UPDATE public.activity SET activitytype='EDIT_COMMISSIONER' WHERE activitytype='EDITAR_COMISIONADO';
UPDATE public.activity SET activitytype='REMOVE_COMMISSIONER' WHERE activitytype='ELIMINAR_COMISIONADO';
UPDATE public.activity SET activitytype='ADD_COMMISSIONER' WHERE activitytype='AGREGAR_COMISIONADO';
UPDATE public.activity SET activitytype='CREATE_ELECTION' WHERE activitytype='CREAR_ELECCION';
UPDATE public.activity SET activitytype='LOAD_CENSUS' WHERE activitytype='CARGAR_PADRON';
UPDATE public.activity SET activitytype='LOAD_CANDIDATES' WHERE activitytype='CARGAR_CANDIDATOS';
UPDATE public.activity SET activitytype='LOAD_AUDITORS' WHERE activitytype='CARGAR_AUDITORES';
UPDATE public.activity SET activitytype='DELETE_ELECTION' WHERE activitytype='ELIMINAR_ELECCION';
UPDATE public.activity SET activitytype='EDIT_ELECTION' WHERE activitytype='EDITAR_ELECCION';
UPDATE public.activity SET activitytype='EDIT_CENSUS' WHERE activitytype='EDITAR_PADRON';
UPDATE public.activity SET activitytype='EDIT_CANDIDATES' WHERE activitytype='EDITAR_CANDIDATOS';
UPDATE public.activity SET activitytype='EDIT_AUDITOR' WHERE activitytype='EDITAR_AUDITOR';
UPDATE public.activity SET activitytype='ENABLE_VOTE_LINK' WHERE activitytype='HABILITACION_LINK_VOTACION';
UPDATE public.activity SET activitytype='ENABLE_RESULTS_LINK' WHERE activitytype='HABILITACION_LINK_RESULTADOS';
UPDATE public.activity SET activitytype='ENABLE_AUDIT_LINK' WHERE activitytype='HABILITACION_LINK_AUDITORIA';
UPDATE public.activity SET activitytype='ELECTION_REVISION' WHERE activitytype='REVISION_DE_ELECCION';
UPDATE public.activity SET activitytype='ELECTION_REVISION_YES' WHERE activitytype='REVISION_DE_ELECCION_SI';
UPDATE public.activity SET activitytype='ELECTION_REVISION_NO' WHERE activitytype='REVISION_DE_ELECCION_NO';
UPDATE public.activity SET activitytype='ENTER_TO_REVISION' WHERE activitytype='INGRESO_A_REVISION';
UPDATE public.activity SET activitytype='ADD_ADMIN' WHERE activitytype='AGREGAR_ADMIN';
UPDATE public.activity SET activitytype='EDIT_ADMIN' WHERE activitytype='EDITAR_ADMIN';
UPDATE public.activity SET activitytype='REMOVE_ADMIN' WHERE activitytype='ELIMINAR_ADMIN';
UPDATE public.activity SET activitytype='ADD_VOTE_USER' WHERE activitytype='AGREGAR_USUARIO_PADRON';
UPDATE public.activity SET activitytype='EDIT_VOTE_USER' WHERE activitytype='EDITAR_USUARIO_PADRON';
UPDATE public.activity SET activitytype='UPDATE_TOKEN_USER_CENSUS' WHERE activitytype='ACTUALIZAR_TOKEN_USUPADRON';
UPDATE public.activity SET activitytype='RESEND_EMAIL_ELECTION_USER_CENSUS' WHERE activitytype='REENVIAR_EMAIL_ELECCION_USUPADRON';
UPDATE public.activity SET activitytype='REMOVE_USER_CENSUS' WHERE activitytype='ELIMINAR_USUPADRON';
UPDATE public.activity SET activitytype='ADD_CENSUS' WHERE activitytype='AGREGAR_PADRON';
UPDATE public.activity SET activitytype='REMOVE_CANDIDATE' WHERE activitytype='ELIMINAR_CANDIDATO';
UPDATE public.activity SET activitytype='ADD_CANDIDATES' WHERE activitytype='AGREGAR_CANDIDATOS';
UPDATE public.activity SET activitytype='ADD_CANDIDATE' WHERE activitytype='AGREGAR_CANDIDATO';
UPDATE public.activity SET activitytype='ADD_AUDITOR' WHERE activitytype='AGREGAR_AUDITOR';
UPDATE public.activity SET activitytype='ADD_AUDITORS' WHERE activitytype='AGREGAR_AUDITORES';
UPDATE public.activity SET activitytype='REMOVE_AUDITOR' WHERE activitytype='ELIMINAR_AUDITOR';
UPDATE public.activity SET activitytype='ADD_PARAMETER' WHERE activitytype='AGREGAR_PARAMETRO';
UPDATE public.activity SET activitytype='EDIT_PARAMETER' WHERE activitytype='EDITAR_PARAMETRO';
UPDATE public.activity SET activitytype='DELETE_PARAMETER' WHERE activitytype='ELIMINAR_PARAMETRO';
UPDATE public.activity SET activitytype='ADD_BASE_TEMPLATE' WHERE activitytype='AGREGAR_TEMPLATE_BASE';
