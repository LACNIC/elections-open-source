INSERT INTO electionemailtemplate (electionemailtemplate_id,subjecten,subjectsp,subjectpt,bodyen,bodysp,bodypt,"type",election_id) VALUES
	 (9,'','','','','','','NEW',NULL),
	 (2,'[Elections] $election.titleEnglish','[Elecciones] $election.titleSpanish','[Eleições] $election.titlePortuguese','Dear,

The auditor $auditor.name, is in agreement with the $election.titleEnglish

','Estimados,

El auditor $auditor.name, ha indicado su conformidad con $election.titleSpanish

','Prezados,

O(a) auditor(a) $auditor.name, está de acordo com a $election.titlePortuguese

','AUDITOR_AGREEMENT',NULL),
	 (7,'[Elections] $election.titleEnglish','[Elecciones] $election.titleSpanish','[Eleições] $election.titlePortuguese','Dear $user.name,

The following $election.startDateString the $election.titleEnglish will begin.

From that moment on you will be able to access the link below and vote:

$user.voteLink

','Estimado/a $user.name,

El próximo día $election.startDateString comenzará la $election.titleSpanish.

A partir de ese momento usted podrá acceder al siguiente enlace y votar:

$user.voteLink

','Prezado(a) $user.name,

No próximo dia $election.startDateString começará a $election.titlePortuguese.

A partir desse momento você poderá acessar ao seguinte link e votar:

$user.voteLink

','ELECTION_NOTICE',NULL),
	 (12,'[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $user.name,

El proximo dia $election.startDateString comenzará la $election.titleSpanish.

A partir de ese momento usted podrá acceder al siguiente enlace y votar:
$user.voteLink

--

Dear $user.name,

The following $election.startDateString the $election.titleEnglish will begin.

From that moment on you will be able to access the link below and vote:
$user.voteLink

--

Prezado(a) $user.name,

No próximo dia $election.startDateString começará a $election.titlePortuguese.

A partir desse momento você poderá acessar ao seguinte link e votar:
$user.voteLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $user.name,

El proximo dia $election.startDateString comenzará la $election.titleSpanish.

A partir de ese momento usted podrá acceder al siguiente enlace y votar:
$user.voteLink

--

Dear $user.name,

The following $election.startDateString the $election.titleEnglish will begin.

From that moment on you will be able to access the link below and vote:
$user.voteLink

--

Prezado(a) $user.name,

No próximo dia $election.startDateString começará a $election.titlePortuguese.

A partir desse momento você poderá acessar ao seguinte link e votar:
$user.voteLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $user.name,

El proximo dia $election.startDateString comenzará la $election.titleSpanish.

A partir de ese momento usted podrá acceder al siguiente enlace y votar:
$user.voteLink

--

Dear $user.name,

The following $election.startDateString the $election.titleEnglish will begin.

From that moment on you will be able to access the link below and vote:
$user.voteLink

--

Prezado(a) $user.name,

No próximo dia $election.startDateString começará a $election.titlePortuguese.

A partir desse momento você poderá acessar ao seguinte link e votar:
$user.voteLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','ELECTION_NOTICE_SP_PT_EN',NULL),
	 (11,'[Elecciones][Elections][Eleições] Auditoría / Audit / Auditoria','[Elecciones][Elections][Eleições] Auditoría / Audit / Auditoria','[Elecciones][Elections][Eleições] Auditoría / Audit / Auditoria','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Para visualizar los resultados y auditar los votos de
$election.titleSpanish

sirvase ingresar en el siguiente enlace:
$auditor.resultLink

--
Dear $auditor.name,

To view the results and audit of the votes of
$election.titleEnglish

please go the following link:
$auditor.resultLink

--

Prezado(a) $auditor.name,

Para visualizar os resultados e auditar os votos da
$election.titlePortuguese

por favor, ingresse ao seguinte link:
$auditor.resultLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Para visualizar los resultados y auditar los votos de
$election.titleSpanish

sirvase ingresar en el siguiente enlace:
$auditor.resultLink

--
Dear $auditor.name,

To view the results and audit of the votes of
$election.titleEnglish

please go the following link:
$auditor.resultLink

--

Prezado(a) $auditor.name,

Para visualizar os resultados e auditar os votos da
$election.titlePortuguese

por favor, ingresse ao seguinte link:
$auditor.resultLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Para visualizar los resultados y auditar los votos de
$election.titleSpanish

sirvase ingresar en el siguiente enlace:
$auditor.resultLink

--
Dear $auditor.name,

To view the results and audit of the votes of
$election.titleEnglish

please go the following link:
$auditor.resultLink

--

Prezado(a) $auditor.name,

Para visualizar os resultados e auditar os votos da
$election.titlePortuguese

por favor, ingresse ao seguinte link:
$auditor.resultLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','AUDITOR_SP_PT_EN',NULL),
	 (14,'[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $user.name,

Queda poco tiempo para paticipar en la
$election.titleSpanish

recuerde que usted puede acceder al siguiente enlace y votar:
$user.voteLink

La fecha limite para votar es $election.endDateString

--

Dear $user.name,

There is no much more time left to participate in this
$election.titleEnglish

Remember that you can access the following link and vote:
$user.voteLink

The deadline to vote is $election.endDateString

--

Prezado(a) $user.name,

Resta pouco tempo para participar na
$election.titlePortuguese

Lembre-se que você pode acessar ao seguinte link e votar:
$user.voteLink

A data limite para votar È $election.endDateString

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $user.name,

Queda poco tiempo para paticipar en la
$election.titleSpanish

recuerde que usted puede acceder al siguiente enlace y votar:
$user.voteLink

La fecha limite para votar es $election.endDateString

--

Dear $user.name,

There is no much more time left to participate in this
$election.titleEnglish

Remember that you can access the following link and vote:
$user.voteLink

The deadline to vote is $election.endDateString

--

Prezado(a) $user.name,

Resta pouco tempo para participar na
$election.titlePortuguese

Lembre-se que você pode acessar ao seguinte link e votar:
$user.voteLink

A data limite para votar È $election.endDateString

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $user.name,

Queda poco tiempo para paticipar en la
$election.titleSpanish

recuerde que usted puede acceder al siguiente enlace y votar:
$user.voteLink

La fecha limite para votar es $election.endDateString

--

Dear $user.name,

There is no much more time left to participate in this
$election.titleEnglish

Remember that you can access the following link and vote:
$user.voteLink

The deadline to vote is $election.endDateString

--

Prezado(a) $user.name,

Resta pouco tempo para participar na
$election.titlePortuguese

Lembre-se que você pode acessar ao seguinte link e votar:
$user.voteLink

A data limite para votar È $election.endDateString

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','ELECTION_ABOUT_TO_END_SP_PT_EN',NULL),
	 (15,'[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $user.name,

Para visualizar los resultados y verificar sus votos de la $election.titleSpanish, sirvase ingresar en el siguiente enlace

$election.resultLink

--

Dear $user.name,

To view the results and verify your votes from $election.titleEnglish, please, go to the following link:

$election.resultLink

--

Prezado(a) $user.name,

Para visualizar os resultados e verificar seus votos da $election.titlePortuguese, por favor, ingresse ao seguinte link:

$election.resultLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $user.name,

Para visualizar los resultados y verificar sus votos de la $election.titleSpanish, sirvase ingresar en el siguiente enlace

$election.resultLink

--

Dear $user.name,

To view the results and verify your votes from $election.titleEnglish, please, go to the following link:

$election.resultLink

--

Prezado(a) $user.name,

Para visualizar os resultados e verificar seus votos da $election.titlePortuguese, por favor, ingresse ao seguinte link:

$election.resultLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','[English below]
[Português abaixo]

Estimado/a $user.name,

Para visualizar los resultados y verificar sus votos de la $election.titleSpanish, sirvase ingresar en el siguiente enlace

$election.resultLink

--

Dear $user.name,

To view the results and verify your votes from $election.titleEnglish, please, go to the following link:

$election.resultLink

--

Prezado(a) $user.name,

Para visualizar os resultados e verificar seus votos da $election.titlePortuguese, por favor, ingresse ao seguinte link:

$election.resultLink

--
la organización
voting@example.org
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','VOTE_RESULT_SP_PT_EN',NULL),
	 (10,'[Elections] $election.titleEnglish','[Elecciones] $election.titleSpanish','[Eleições] $election.titlePortuguese','Dear,

The auditor $auditor.name, has enabled revision for election: $election.titleEnglish','Estimados,

El auditor $auditor.name, ha habilitado la revisión de la elección: $election.titleSpanish','Prezados,

No(a) auditor(a) $auditor.name, habilitou a revisão da eleição: $election.titlePortuguese','AUDITOR_REVISION',NULL),
	 (4,'[Elections] Verification Codes','[Elecciones] Códigos de verificación','[Eleições] Códigos de verificação','Dear $user.name,

Thank you for your participation in the $election.titleEnglish

We received your vote correctly and we are sending the verification codes below:

CODE/CANDIDATE

----------------------------------------

$user.codesSummary

From $election.endDateString forward, you will be able to access the following link and see the results of the election:

$election.resultLink

','Estimado/a $user.name,

Gracias por su participación en la $election.titleSpanish

Hemos recibido su voto correctamente y a continuación le enviamos los codigos de verificación:

CODIGO/CANDIDATO

----------------------------------------

$user.codesSummary

A partir del día $election.endDateString podrá acceder al siguiente enlace y ver los resultados de la elección:

$election.resultLink

','Prezado(a)  $user.name,

Agradecemos a sua participação na $election.titlePortuguese.

Recebemos corretamente o seu voto e a continuação lhe enviamos os códigos de verificação:

CÓDIGO/CANDIDATO

----------------------------------------

$user.codesSummary

A partir do dia $election.endDateString você poderá acessar ao seguinte link e ver os resultados da eleição:

$election.resultLink

','VOTE_CODES',NULL),
	 (3,'[Elections] Audit','[Elecciones] Auditoría','[Eleições] Auditoria','Dear $auditor.name,

To view the results and audit the votes of

$election.titleEnglish

Please go to the following link:

$auditor.resultLink

','Estimado/a $auditor.name,

Para visualizar los resultados y auditar los votos de

$election.titleSpanish

Por favor, ingresar en el siguiente enlace

$auditor.resultLink

','Prezado(a) $auditor.name,

Para visualizar os resultados e auditar os votos da

$election.titlePortuguese

Por favor, ingresse ao seguinte link:

$auditor.resultLink

','AUDITOR',NULL);
INSERT INTO electionemailtemplate (electionemailtemplate_id,subjecten,subjectsp,subjectpt,bodyen,bodysp,bodypt,"type",election_id) VALUES
	 (5,'[Elections] $election.titleEnglish','[Elecciones] $election.titleSpanish','[Eleições] $election.titlePortuguese','Dear $user.name,

There is no much more time left to participate in this $election.titleEnglish

Remember that you can access the following link and vote:

$user.voteLink

The deadline to vote is $election.endDateString

','Estimado/a $user.name,

Queda poco tiempo para paticipar en la $election.titleSpanish

Recuerde que usted puede acceder al siguiente enlace y votar:

$user.voteLink

La fecha limite para votar es $election.endDateString

','Prezado(a) $user.name,

Resta pouco tempo para participar na $election.titlePortuguese

Lembre-se que vocÍ pode acessar ao seguinte link e votar:

$user.voteLink

A data limite para votar è $election.endDateString

','ELECTION_ABOUT_TO_END',NULL),
	 (8,'[Elections] Results','[Elecciones] Resultados','[Eleições] Resultados','Dear $user.name,

To view the results and verify your votes from $election.titleEnglish, please go to the following link:

$election.resultLink

','Estimado/a $user.name,

Para visualizar los resultados y verificar sus votos de la $election.titleSpanish, por favor ingrese en el siguiente enlace:

$election.resultLink

','Prezado(a) $user.name,

Para visualizar os resultados e verificar seus votos da $election.titlePortuguese, por favor ingresse ao seguinte link:

$election.resultLink

','VOTE_RESULT',NULL),
	 (1,'[Elections] $election.titleEnglish','[Elecciones] $election.titleSpanish','[Eleições] $election.titlePortuguese','Dear $user.name,

From this moment on your link is enabled to participate in

$election.titleEnglish.

You can now access it below and vote:

$user.voteLink


','Estimado/a $user.name,

En este momento está activo su enlace para participar en

$election.titleSpanish

A partir de ahora usted podrá acceder al siguiente enlace y votar

$user.voteLink


','Prezado(a) $user.name,

A partir deste momento está ativado o seu link para participar na

$election.titlePortuguese

Você já pode acessá-lo abaixo e votar:

$user.voteLink


','ELECTION_START',NULL),
	 (2391,'[Elecciones][Elections][Eleições] Nominación / Nomination / Nomeação','[Elecciones][Elections][Eleições] Nominación / Nomination / Nomeação','[Elecciones][Elections][Eleições] Nominación / Nomination / Nomeação','[English below]
[Português abaixo]

Estimado/a $nomination.nominationName,

Le informamos que ha sido nominado/a como candidato/a para la elección:

$election.titleSpanish

#if($organization.name)
La organización que realizó la nominación es: $organization.name
#end

Para aceptar o rechazar esta nominación, por favor ingrese en el siguiente enlace:

$nomination.acceptNominationLink

Si usted no desea postularse, puede rechazar la nominación desde ese mismo enlace.

--
Dear $nomination.nominationName,

You have been nominated as a candidate for the election:

$election.titleEnglish

#if($organization.name)
The organization that submitted the nomination is: $organization.name
#end

To accept or reject this nomination, please access the following link:

$nomination.acceptNominationLink

If you do not wish to run, you can reject the nomination from the same link.

--
Prezado(a) $nomination.nominationName,

Informamos que você foi nomeado(a) como candidato(a) para a eleição:

$election.titlePortuguese

#if($organization.name)
A organização que realizou a nomeação é: $organization.name
#end

Para aceitar ou rejeitar esta nomeação, por favor acesse o seguinte link:

$nomination.acceptNominationLink

Se você não deseja se candidatar, pode rejeitar a nomeação pelo mesmo link.','[English below]
[Português abaixo]

Estimado/a $nomination.nominationName,

Le informamos que ha sido nominado/a como candidato/a para la elección:

$election.titleSpanish

#if($organization.name)
La organización que realizó la nominación es: $organization.name
#end

Para aceptar o rechazar esta nominación, por favor ingrese en el siguiente enlace:

$nomination.acceptNominationLink

Si usted no desea postularse, puede rechazar la nominación desde ese mismo enlace.

--
Dear $nomination.nominationName,

You have been nominated as a candidate for the election:

$election.titleEnglish

#if($organization.name)
The organization that submitted the nomination is: $organization.name
#end

To accept or reject this nomination, please access the following link:

$nomination.acceptNominationLink

If you do not wish to run, you can reject the nomination from the same link.

--
Prezado(a) $nomination.nominationName,

Informamos que você foi nomeado(a) como candidato(a) para a eleição:

$election.titlePortuguese

#if($organization.name)
A organização que realizou a nomeação é: $organization.name
#end

Para aceitar ou rejeitar esta nomeação, por favor acesse o seguinte link:

$nomination.acceptNominationLink

Se você não deseja se candidatar, pode rejeitar a nomeação pelo mesmo link.','[English below]
[Português abaixo]

Estimado/a $nomination.nominationName,

Le informamos que ha sido nominado/a como candidato/a para la elección:

$election.titleSpanish

#if($organization.name)
La organización que realizó la nominación es: $organization.name
#end

Para aceptar o rechazar esta nominación, por favor ingrese en el siguiente enlace:

$nomination.acceptNominationLink

Si usted no desea postularse, puede rechazar la nominación desde ese mismo enlace.

--
Dear $nomination.nominationName,

You have been nominated as a candidate for the election:

$election.titleEnglish

#if($organization.name)
The organization that submitted the nomination is: $organization.name
#end

To accept or reject this nomination, please access the following link:

$nomination.acceptNominationLink

If you do not wish to run, you can reject the nomination from the same link.

--
Prezado(a) $nomination.nominationName,

Informamos que você foi nomeado(a) como candidato(a) para a eleição:

$election.titlePortuguese

#if($organization.name)
A organização que realizou a nomeação é: $organization.name
#end

Para aceitar ou rejeitar esta nomeação, por favor acesse o seguinte link:

$nomination.acceptNominationLink

Se você não deseja se candidatar, pode rejeitar a nomeação pelo mesmo link.','NOMINATION_SUBMITTED_NOMINEE',NULL),
	 (2392,'[Elections] Nomination accepted - $election.titleEnglish','[Elecciones] Nominación aceptada - $election.titleSpanish','[Eleições] Nomeação aceita - $election.titlePortuguese','Dear $organization.membershipContactName,

We inform you that $nomination.nominationName has accepted the nomination for the election:

$election.titleEnglish

#if($candidate.name)
Candidate: $candidate.name
#end
#if($nomination.nominationEmail)
Contact email: $nomination.nominationEmail
#end

Best regards.','Estimado/a $organization.membershipContactName,

Le informamos que $nomination.nominationName ha aceptado la nominación para la elección:

$election.titleSpanish

#if($candidate.name)
Candidato/a: $candidate.name
#end
#if($nomination.nominationEmail)
Correo de contacto: $nomination.nominationEmail
#end

Saludos cordiales.','Prezado(a) $organization.membershipContactName,

Informamos que $nomination.nominationName aceitou a nomeação para a eleição:

$election.titlePortuguese

#if($candidate.name)
Candidato(a): $candidate.name
#end
#if($nomination.nominationEmail)
E-mail de contato: $nomination.nominationEmail
#end

Atenciosamente.','NOMINATION_ACCEPTED_REPRESENTATIVE',NULL),
	 (2393,'[Elections] Nomination rejected - $election.titleEnglish','[Elecciones] Nominación rechazada - $election.titleSpanish','[Eleições] Nomeação rejeitada - $election.titlePortuguese','Dear $organization.membershipContactName,

We inform you that $nomination.nominationName has rejected the nomination for the election:

$election.titleEnglish

#if($nomination.nominationEmail)
Contact email: $nomination.nominationEmail
#end

Best regards.','Estimado/a $organization.membershipContactName,

Le informamos que $nomination.nominationName ha rechazado la nominación para la elección:

$election.titleSpanish

#if($nomination.nominationEmail)
Correo de contacto: $nomination.nominationEmail
#end

Saludos cordiales.','Prezado(a) $organization.membershipContactName,

Informamos que $nomination.nominationName rejeitou a nomeação para a eleição:

$election.titlePortuguese

#if($nomination.nominationEmail)
E-mail de contato: $nomination.nominationEmail
#end

Atenciosamente.','NOMINATION_REJECTED_REPRESENTATIVE',NULL),
	 (2395,'[Elections] Candidacy support request - $election.titleEnglish','[Elecciones] Solicitud de apoyo a candidatura - $election.titleSpanish','[Eleições] Solicitação de apoio à candidatura - $election.titlePortuguese','Dear #if($supportNomination.supportingContactName)$supportNomination.supportingContactName#{else}#end,

The candidate applicant for the election:

$election.titleEnglish

is requesting your support for their candidacy.

Applicant details:
Name: #if($candidate.name)$candidate.name#{else}$nomination.nominationName#end
Email: $nomination.nominationEmail
#if($nomination.nominationPhoneNumber)
Phone: $nomination.nominationPhoneNumber
#end
#if($organization.name)
Nominating organization: $organization.name
#end

#if($candidate.bioEnglish)
Biography:
$candidate.bioEnglish
#end

#if($nomination.nominationReasonEnglish)
Nominated by:
$nomination.nominationReasonEnglish
#end

To submit your decision regarding this support request, please access the following link:

$supportNomination.supportNominationLink

Best regards.','Estimado/a #if($supportNomination.supportingContactName)$supportNomination.supportingContactName#{else}#end,

El aspirante a candidato/a para la elección:

$election.titleSpanish

solicita su apoyo a su candidatura.

Datos del aspirante:
Nombre: #if($candidate.name)$candidate.name#{else}$nomination.nominationName#end
Correo: $nomination.nominationEmail
#if($nomination.nominationPhoneNumber)
Teléfono: $nomination.nominationPhoneNumber
#end
#if($organization.name)
Organización nominadora: $organization.name
#end

#if($candidate.bioSpanish)
Biografía:
$candidate.bioSpanish
#end

#if($nomination.nominationReasonSpanish)
Nominado por:
$nomination.nominationReasonSpanish
#end

Para registrar su decisión sobre esta solicitud de apoyo, por favor ingrese en el siguiente enlace:

$supportNomination.supportNominationLink

Saludos cordiales.','Prezado(a) #if($supportNomination.supportingContactName)$supportNomination.supportingContactName#{else}#end,

O aspirante a candidato(a) para a eleição:

$election.titlePortuguese

solicita seu apoio à sua candidatura.

Dados do aspirante:
Nome: #if($candidate.name)$candidate.name#{else}$nomination.nominationName#end
E-mail: $nomination.nominationEmail
#if($nomination.nominationPhoneNumber)
Telefone: $nomination.nominationPhoneNumber
#end
#if($organization.name)
Organização nomeadora: $organization.name
#end

#if($candidate.bioPortuguese)
Biografia:
$candidate.bioPortuguese
#end

#if($nomination.nominationReasonPortuguese)
Nomeado por:
$nomination.nominationReasonPortuguese
#end

Para registrar sua decisão sobre esta solicitação de apoio, por favor acesse o seguinte link:

$supportNomination.supportNominationLink

Atenciosamente.','NOMINATION_USER_SUPPORT_REQUEST',NULL),
	 (2394,'[Elections] Candidacy support request - $election.titleEnglish','[Elecciones] Solicitud de apoyo a candidatura - $election.titleSpanish','[Eleições] Solicitação de apoio à candidatura - $election.titlePortuguese','Dear $supportingOrganization.membershipContactName,

The candidate applicant for the election:

$election.titleEnglish

is requesting your organization’s support for their candidacy.

Support is being requested from you, $supportingOrganization.membershipContactName, as the organization-associated membership contact.

Organization from which support is requested:
#if($supportingOrganization.name)
Organization: $supportingOrganization.name
#end
#if($supportingOrganization.orgId)
Org ID: $supportingOrganization.orgId
#end
#if($supportingOrganization.membershipContactName)
Membership contact: $supportingOrganization.membershipContactName
#end
#if($supportingOrganization.membershipContactEmail)
Membership contact email: $supportingOrganization.membershipContactEmail
#end

Applicant details:
Name: #if($candidate.name)$candidate.name#{else}$nomination.nominationName#end
Email: $nomination.nominationEmail
#if($nomination.nominationPhoneNumber)
Phone: $nomination.nominationPhoneNumber
#end
#if($organization.name)
Nominating organization: $organization.name
#end

#if($candidate.bioEnglish)
Biography:
$candidate.bioEnglish
#end

#if($nomination.nominationReasonEnglish)
Nominated by:
$nomination.nominationReasonEnglish
#end

To submit your decision regarding this support request, please access the following link:

$supportNomination.supportNominationLink

Best regards.','Estimado/a $supportingOrganization.membershipContactName,

El aspirante a candidato/a para la elección:

$election.titleSpanish

solicita el apoyo de su organización a su candidatura.

Se le solicita apoyo a su candidatura a usted, $supportingOrganization.membershipContactName, como contacto de membresía asociado a la organización.

Organización a la que se solicita apoyo:
#if($supportingOrganization.name)
Organización: $supportingOrganization.name
#end
#if($supportingOrganization.orgId)
Org ID: $supportingOrganization.orgId
#end
#if($supportingOrganization.membershipContactName)
Contacto de membresía: $supportingOrganization.membershipContactName
#end
#if($supportingOrganization.membershipContactEmail)
Correo del contacto de membresía: $supportingOrganization.membershipContactEmail
#end

Datos del aspirante:
Nombre: #if($candidate.name)$candidate.name#{else}$nomination.nominationName#end
Correo: $nomination.nominationEmail
#if($nomination.nominationPhoneNumber)
Teléfono: $nomination.nominationPhoneNumber
#end
#if($organization.name)
Organización nominadora: $organization.name
#end

#if($candidate.bioSpanish)
Biografía:
$candidate.bioSpanish
#end

#if($nomination.nominationReasonSpanish)
Nominado por:
$nomination.nominationReasonSpanish
#end

Para registrar su decisión sobre esta solicitud de apoyo, por favor ingrese en el siguiente enlace:

$supportNomination.supportNominationLink

Saludos cordiales.','Prezado(a) $supportingOrganization.membershipContactName,

O aspirante a candidato(a) para a eleição:

$election.titlePortuguese

solicita o apoio da sua organização à sua candidatura.

O apoio à candidatura está sendo solicitado a você, $supportingOrganization.membershipContactName, como contato de membresia associado à la organización.

Organização da qual se solicita apoio:
#if($supportingOrganization.name)
Organização: $supportingOrganization.name
#end
#if($supportingOrganization.orgId)
Org ID: $supportingOrganization.orgId
#end
#if($supportingOrganization.membershipContactName)
Contato de membresia: $supportingOrganization.membershipContactName
#end
#if($supportingOrganization.membershipContactEmail)
E-mail do contato de membresia: $supportingOrganization.membershipContactEmail
#end

Dados do aspirante:
Nome: #if($candidate.name)$candidate.name#{else}$nomination.nominationName#end
E-mail: $nomination.nominationEmail
#if($nomination.nominationPhoneNumber)
Telefone: $nomination.nominationPhoneNumber
#end
#if($organization.name)
Organização nomeadora: $organization.name
#end

#if($candidate.bioPortuguese)
Biografia:
$candidate.bioPortuguese
#end

#if($nomination.nominationReasonPortuguese)
Nomeado por:
$nomination.nominationReasonPortuguese
#end

Para registrar sua decisão sobre esta solicitação de apoio, por favor acesse o seguinte link:

$supportNomination.supportNominationLink

Atenciosamente.','NOMINATION_ORG_SUPPORT_REQUEST',NULL),
	 (2396,'[Elecciones][Elections][Eleições] Apoyo recibido / Support received / Apoio recebido','[Elecciones][Elections][Eleições] Apoyo recibido / Support received / Apoio recebido','[Elecciones][Elections][Eleições] Apoyo recibido / Support received / Apoio recebido','[English below]
[Português abaixo]

Estimado/a #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Le informamos que su solicitud de apoyo a candidatura fue aprobada por:

Organización de apoyo: $!supportingOrganization.name
Contacto de apoyo: $!supportNomination.supportingContactName
Estado del apoyo: $!supportNomination.supportStatus

Esta notificación es informativa. Su candidatura continúa su curso normal.

--
Dear #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

We inform you that your candidacy support request was approved by:

Supporting organization: $!supportingOrganization.name
Support contact: $!supportNomination.supportingContactName
Support status: $!supportNomination.supportStatus

This notification is for information purposes. Your candidacy continues under the normal process.

--
Prezado(a) #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Informamos que sua solicitação de apoio à candidatura foi aprovada por:

Organização de apoio: $!supportingOrganization.name
Contato de apoio: $!supportNomination.supportingContactName
Status do apoio: $!supportNomination.supportStatus

Esta notificação é informativa. Sua candidatura continua com o fluxo normal.','[English below]
[Português abaixo]

Estimado/a #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Le informamos que su solicitud de apoyo a candidatura fue aprobada por:

Organización de apoyo: $!supportingOrganization.name
Contacto de apoyo: $!supportNomination.supportingContactName
Estado del apoyo: $!supportNomination.supportStatus

Esta notificación es informativa. Su candidatura continúa su curso normal.

--
Dear #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

We inform you that your candidacy support request was approved by:

Supporting organization: $!supportingOrganization.name
Support contact: $!supportNomination.supportingContactName
Support status: $!supportNomination.supportStatus

This notification is for information purposes. Your candidacy continues under the normal process.

--
Prezado(a) #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Informamos que sua solicitação de apoio à candidatura foi aprovada por:

Organização de apoio: $!supportingOrganization.name
Contato de apoio: $!supportNomination.supportingContactName
Status do apoio: $!supportNomination.supportStatus

Esta notificação é informativa. Sua candidatura continua com o fluxo normal.','[English below]
[Português abaixo]

Estimado/a #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Le informamos que su solicitud de apoyo a candidatura fue aprobada por:

Organización de apoyo: $!supportingOrganization.name
Contacto de apoyo: $!supportNomination.supportingContactName
Estado del apoyo: $!supportNomination.supportStatus

Esta notificación es informativa. Su candidatura continúa su curso normal.

--
Dear #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

We inform you that your candidacy support request was approved by:

Supporting organization: $!supportingOrganization.name
Support contact: $!supportNomination.supportingContactName
Support status: $!supportNomination.supportStatus

This notification is for information purposes. Your candidacy continues under the normal process.

--
Prezado(a) #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Informamos que sua solicitação de apoio à candidatura foi aprovada por:

Organização de apoio: $!supportingOrganization.name
Contato de apoio: $!supportNomination.supportingContactName
Status do apoio: $!supportNomination.supportStatus

Esta notificação é informativa. Sua candidatura continua com o fluxo normal.','NOMINATION_SUPPORT_STATUS_APPROVED_CANDIDATE',NULL),
	 (2397,'[Elecciones][Elections][Eleições] Apoyo rechazado / Support rejected / Apoio rejeitado','[Elecciones][Elections][Eleições] Apoyo rechazado / Support rejected / Apoio rejeitado','[Elecciones][Elections][Eleições] Apoyo rechazado / Support rejected / Apoio rejeitado','[English below]
[Português abaixo]

Estimado/a #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Le informamos que su solicitud de apoyo a candidatura fue rechazada por:

Organización de apoyo: $!supportingOrganization.name
Contacto de apoyo: $!supportNomination.supportingContactName
Estado del apoyo: $!supportNomination.supportStatus

Si desea continuar su postulación, por favor ingrese nuevamente a su panel y solicite apoyo a otra organización o contacto.

--
Dear #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

We inform you that your candidacy support request was rejected by:

Supporting organization: $!supportingOrganization.name
Support contact: $!supportNomination.supportingContactName
Support status: $!supportNomination.supportStatus

If you wish to continue your application, please go back to your panel and request support from another organization or contact.

--
Prezado(a) #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Informamos que sua solicitação de apoio à candidatura foi rejeitada por:

Organização de apoio: $!supportingOrganization.name
Contato de apoio: $!supportNomination.supportingContactName
Status do apoio: $!supportNomination.supportStatus

Se você deseja continuar sua candidatura, por favor acesse novamente seu painel e solicite apoio a outra organização ou contato.','[English below]
[Português abaixo]

Estimado/a #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Le informamos que su solicitud de apoyo a candidatura fue rechazada por:

Organización de apoyo: $!supportingOrganization.name
Contacto de apoyo: $!supportNomination.supportingContactName
Estado del apoyo: $!supportNomination.supportStatus

Si desea continuar su postulación, por favor ingrese nuevamente a su panel y solicite apoyo a otra organización o contacto.

--
Dear #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

We inform you that your candidacy support request was rejected by:

Supporting organization: $!supportingOrganization.name
Support contact: $!supportNomination.supportingContactName
Support status: $!supportNomination.supportStatus

If you wish to continue your application, please go back to your panel and request support from another organization or contact.

--
Prezado(a) #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Informamos que sua solicitação de apoio à candidatura foi rejeitada por:

Organização de apoio: $!supportingOrganization.name
Contato de apoio: $!supportNomination.supportingContactName
Status do apoio: $!supportNomination.supportStatus

Se você deseja continuar sua candidatura, por favor acesse novamente seu painel e solicite apoio a outra organização ou contato.','[English below]
[Português abaixo]

Estimado/a #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Le informamos que su solicitud de apoyo a candidatura fue rechazada por:

Organización de apoyo: $!supportingOrganization.name
Contacto de apoyo: $!supportNomination.supportingContactName
Estado del apoyo: $!supportNomination.supportStatus

Si desea continuar su postulación, por favor ingrese nuevamente a su panel y solicite apoyo a otra organización o contacto.

--
Dear #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

We inform you that your candidacy support request was rejected by:

Supporting organization: $!supportingOrganization.name
Support contact: $!supportNomination.supportingContactName
Support status: $!supportNomination.supportStatus

If you wish to continue your application, please go back to your panel and request support from another organization or contact.

--
Prezado(a) #if($candidate.name)$!candidate.name#{else}$!nomination.nominationName#end,

Informamos que sua solicitação de apoio à candidatura foi rejeitada por:

Organização de apoio: $!supportingOrganization.name
Contato de apoio: $!supportNomination.supportingContactName
Status do apoio: $!supportNomination.supportStatus

Se você deseja continuar sua candidatura, por favor acesse novamente seu painel e solicite apoio a outra organização ou contato.','NOMINATION_SUPPORT_STATUS_REJECTED_CANDIDATE',NULL);
INSERT INTO electionemailtemplate (electionemailtemplate_id,subjecten,subjectsp,subjectpt,bodyen,bodysp,bodypt,"type",election_id) VALUES
	 (2404,'[Elecciones] Recordatorio de tareas pendientes - $election.titleSpanish','[Elecciones] Recordatorio de tareas pendientes - $election.titleSpanish','[Eleições] Lembrete de tarefas pendentes - $election.titlePortuguese','Dear $candidate.name,

Pending tasks:
$candidatePendingTasks

Completed tasks:
$candidateCompletedTasks

You can continue and complete your nomination at the following link:
$nomination.acceptNominationLink','Estimado/a $candidate.name,

Tareas pendientes:
$candidatePendingTasks

Tareas completadas:
$candidateCompletedTasks

Puede continuar y completar su nominación en el siguiente enlace:
$nomination.acceptNominationLink','Prezado(a) $candidate.name,

Tarefas pendentes:
$candidatePendingTasks

Tarefas concluídas:
$candidateCompletedTasks

Você pode continuar e concluir sua nomeação no seguinte link:
$nomination.acceptNominationLink','CANDIDATE_REMINDER',NULL),
	 (2398,'[Elecciones][Elections][Eleições] Revisión de candidatura / Candidate review / Revisão de candidatura','[Elecciones][Elections][Eleições] Revisión de candidatura / Candidate review / Revisão de candidatura','[Elecciones][Elections][Eleições] Revisión de candidatura / Candidate review / Revisão de candidatura','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Le invitamos a ingresar al siguiente enlace para revisar los candidatos y aprobar o rechazar su participación en la elección:

$election.titleSpanish

$auditor.tokenAuditLink

--
Dear $auditor.name,

We invite you to access the following link to review the candidates and approve or reject their participation in the election:

$election.titleEnglish

$auditor.tokenAuditLink

--
Prezado(a) $auditor.name,

Convidamos você a acessar o seguinte link para revisar os candidatos e aprovar ou rejeitar sua participação na eleição:

$election.titlePortuguese

$auditor.tokenAuditLink','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Le invitamos a ingresar al siguiente enlace para revisar los candidatos y aprobar o rechazar su participación en la elección:

$election.titleSpanish

$auditor.tokenAuditLink

--
Dear $auditor.name,

We invite you to access the following link to review the candidates and approve or reject their participation in the election:

$election.titleEnglish

$auditor.tokenAuditLink

--
Prezado(a) $auditor.name,

Convidamos você a acessar o seguinte link para revisar os candidatos e aprovar ou rejeitar sua participação na eleição:

$election.titlePortuguese

$auditor.tokenAuditLink','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Le invitamos a ingresar al siguiente enlace para revisar los candidatos y aprobar o rechazar su participación en la elección:

$election.titleSpanish

$auditor.tokenAuditLink

--
Dear $auditor.name,

We invite you to access the following link to review the candidates and approve or reject their participation in the election:

$election.titleEnglish

$auditor.tokenAuditLink

--
Prezado(a) $auditor.name,

Convidamos você a acessar o seguinte link para revisar os candidatos e aprovar ou rejeitar sua participação na eleição:

$election.titlePortuguese

$auditor.tokenAuditLink','AUDITOR_NOMINATION_SP_PT_EN',NULL),
	 (2399,'[Elections] Candidate approved by auditor - $election.titleEnglish','[Elecciones] Candidatura aprobada por auditor - $election.titleSpanish','[Eleições] Candidatura aprovada por auditor - $election.titlePortuguese','Dear team,

A candidate approval by an auditor was registered for the election:

$election.titleEnglish

Auditor comment:
#if($auditorDecisionComment)$auditorDecisionComment#{else}No comment#end','Estimados,

Se registró una aprobación de candidatura por parte de un auditor para la elección:

$election.titleSpanish

Comentario del auditor:
#if($auditorDecisionComment)$auditorDecisionComment#{else}Sin comentario#end','Prezados,

Foi registrada uma aprovação de candidatura por um auditor para a eleição:

$election.titlePortuguese

Comentário do auditor:
#if($auditorDecisionComment)$auditorDecisionComment#{else}Sem comentário#end','AUDITOR_CANDIDATE_APPROVED',NULL),
	 (2400,'[Elections] Candidate rejected by auditor - $election.titleEnglish','[Elecciones] Candidatura rechazada por auditor - $election.titleSpanish','[Eleições] Candidatura rejeitada por auditor - $election.titlePortuguese','Dear team,

A candidate rejection by an auditor was registered for the election:

$election.titleEnglish

Auditor comment:
#if($auditorDecisionComment)$auditorDecisionComment#{else}No comment#end','Estimados,

Se registró un rechazo de candidatura por parte de un auditor para la elección:

$election.titleSpanish

Comentario del auditor:
#if($auditorDecisionComment)$auditorDecisionComment#{else}Sin comentario#end','Prezados,

Foi registrada uma rejeição de candidatura por um auditor para a eleição:

$election.titlePortuguese

Comentário do auditor:
#if($auditorDecisionComment)$auditorDecisionComment#{else}Sem comentário#end','AUDITOR_CANDIDATE_REJECTED',NULL),
	 (2402,'[Elections] Candidate task completed - $election.titleEnglish - $completedTaskKey','[Elecciones] Tarea completada por candidato - $election.titleSpanish - $completedTaskKey','[Eleições] Tarefa concluída por candidato - $election.titlePortuguese  - $completedTaskKey','Dear team,

Candidate $candidate.name has completed a task in the election:

$election.titleEnglish

Details:
Candidate ID: $candidate.candidateId
Email: $candidate.mail
Completed task: $completedTaskKey','Estimados,

El candidato $candidate.name ha completado una tarea en la elección:

$election.titleSpanish

Detalle:
Candidato ID: $candidate.candidateId
Correo: $candidate.mail
Tarea completada: $completedTaskKey','Prezados,

O(a) candidato(a) $candidate.name concluiu uma tarefa na eleição:

$election.titlePortuguese

Detalhes:
ID do candidato: $candidate.candidateId
Email: $candidate.mail
Tarefa concluída: $completedTaskKey','CANDIDATE_TASK_COMPLETED_ADMIN',NULL),
	 (2403,'[Elections] Candidate completed all tasks - $election.titleEnglish','[Elecciones] Candidato completó todas las tareas - $election.titleSpanish','[Eleições] Candidato concluiu todas as tarefas - $election.titlePortuguese','Dear team,

Candidate $candidate.name has completed all required tasks in the election:

$election.titleEnglish

Details:
Candidate ID: $candidate.candidateId
Email: $candidate.mail','Estimados,

El candidato $candidate.name ha completado todas las tareas requeridas en la elección:

$election.titleSpanish

Detalle:
Candidato ID: $candidate.candidateId
Correo: $candidate.mail','Prezados,

O(a) candidato(a) $candidate.name concluiu todas as tarefas obrigatórias na eleição:

$election.titlePortuguese

Detalhes:
ID do candidato: $candidate.candidateId
Email: $candidate.mail','CANDIDATE_ALL_TASKS_COMPLETED_ADMIN',NULL),
	 (2405,'[Elections] Pending audit reminder - $election.titleEnglish','[Elecciones] Recordatorio de auditoría pendiente - $election.titleSpanish','[Eleições] Lembrete de auditoria pendente - $election.titlePortuguese','Dear $auditor.name,

Pending review:
$auditorPendingCandidates

Reviewed/completed:
$auditorCompletedCandidates

You can access the following link to continue the audit:
$auditor.resultLink','Estimado/a $auditor.name,

Pendiente de revisar:
$auditorPendingCandidates

Revisado/completado:
$auditorCompletedCandidates

Puede ingresar en el siguiente enlace para continuar con la auditoría:
$auditor.resultLink','Prezado(a) $auditor.name,

Pendente de revisão:
$auditorPendingCandidates

Revisado/concluído:
$auditorCompletedCandidates

Você pode acessar o seguinte link para continuar a auditoria:
$auditor.resultLink','AUDITOR_REMINDER',NULL),
	 (4291,'[Elections] Pending audit reminder - $election.titleEnglish','[Elecciones] Recordatorio de auditoría pendiente - $election.titleSpanish','[Eleições] Lembrete de auditoria pendente - $election.titlePortuguese','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Le recordamos que, para el correcto desarrollo del proceso electoral, es importante que visualice los resultados y audite los votos correspondientes a la elección: $election.titleSpanish.

Por favor, ingresar en el siguiente enlace:
$auditor.resultLink

--
Dear $auditor.name,

We remind you that, for the proper development of the electoral process, it is important that you view the results and audit the votes corresponding to the election: $election.titleEnglish.

Please access the following link:
$auditor.resultLink

--
Prezado(a) $auditor.name,
Lembramos que, para o correto desenvolvimento do processo eleitoral, é importante que você visualize os resultados e audite os votos correspondentes à eleição: $election.titlePortuguese.

Por favor, acesse o seguinte link:
$auditor.resultLink','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Le recordamos que, para el correcto desarrollo del proceso electoral, es importante que visualice los resultados y audite los votos correspondientes a la elección: $election.titleSpanish.

Por favor, ingresar en el siguiente enlace:
$auditor.resultLink

--
Dear $auditor.name,

We remind you that, for the proper development of the electoral process, it is important that you view the results and audit the votes corresponding to the election: $election.titleEnglish.

Please access the following link:
$auditor.resultLink

--
Prezado(a) $auditor.name,
Lembramos que, para o correto desenvolvimento do processo eleitoral, é importante que você visualize os resultados e audite os votos correspondentes à eleição: $election.titlePortuguese.

Por favor, acesse o seguinte link:
$auditor.resultLink','[English below]
[Português abaixo]

Estimado/a $auditor.name,

Le recordamos que, para el correcto desarrollo del proceso electoral, es importante que visualice los resultados y audite los votos correspondientes a la elección: $election.titleSpanish.

Por favor, ingresar en el siguiente enlace:
$auditor.resultLink

--
Dear $auditor.name,

We remind you that, for the proper development of the electoral process, it is important that you view the results and audit the votes corresponding to the election: $election.titleEnglish.

Please access the following link:
$auditor.resultLink

--
Prezado(a) $auditor.name,
Lembramos que, para o correto desenvolvimento do processo eleitoral, é importante que você visualize os resultados e audite os votos correspondentes à eleição: $election.titlePortuguese.

Por favor, acesse o seguinte link:
$auditor.resultLink','AUDITOR_REMINDER_REVISION',NULL),
	 (2169,'[Aviso interno] $!standardNotice.eventType - enviado a $!standardNotice.targetRecipient','[Aviso interno] $!standardNotice.eventType - enviado a $!standardNotice.targetRecipient','[Aviso interno] $!standardNotice.eventType - enviado a $!standardNotice.targetRecipient','Se registró un envío de correo a un tercero.

Elección: $!standardNotice.electionId#if($election.titleSpanish) - $!election.titleSpanish#end
Nominación: $!standardNotice.nominationId#if($nomination.nominationName) - $!nomination.nominationName#end
Candidato: $!standardNotice.candidateId#if($candidate.name) - $!candidate.name#end
Auditor: $!standardNotice.auditorId#if($auditor.name) - $!auditor.name#end
#if($organization.id || $organization.name)
Organización: $!organization.id#if($organization.name) - $!organization.name#end
#end
#if($standardNotice.supportNominationId || $supportingOrganization.name || $supportNomination.supportingContactName)
Apoyo: $!standardNotice.supportNominationId#if($supportingOrganization.name) - $!supportingOrganization.name#end#if($supportNomination.supportingContactName) (Contacto: $!supportNomination.supportingContactName)#end
#end

Evento: $!standardNotice.eventType
Fecha: $!standardNotice.createdAt

Template estándar: $!standardNotice.standardTemplateType
Destinatario estándar: $!standardNotice.standardRecipient

Template destino: $!standardNotice.targetTemplateType
Template destino encontrado: $!standardNotice.targetTemplateFound
Destinatario destino: $!standardNotice.targetRecipient
Idioma destino: $!standardNotice.targetLanguage
Sender destino: $!standardNotice.targetSender

Asunto enviado:
$!standardNotice.targetSubject','Se registró un envío de correo a un tercero.

Elección: $!standardNotice.electionId#if($election.titleSpanish) - $!election.titleSpanish#end
Nominación: $!standardNotice.nominationId#if($nomination.nominationName) - $!nomination.nominationName#end
Candidato: $!standardNotice.candidateId#if($candidate.name) - $!candidate.name#end
Auditor: $!standardNotice.auditorId#if($auditor.name) - $!auditor.name#end
#if($organization.id || $organization.name)
Organización: $!organization.id#if($organization.name) - $!organization.name#end
#end
#if($standardNotice.supportNominationId || $supportingOrganization.name || $supportNomination.supportingContactName)
Apoyo: $!standardNotice.supportNominationId#if($supportingOrganization.name) - $!supportingOrganization.name#end#if($supportNomination.supportingContactName) (Contacto: $!supportNomination.supportingContactName)#end
#end

Evento: $!standardNotice.eventType
Fecha: $!standardNotice.createdAt

Template estándar: $!standardNotice.standardTemplateType
Destinatario estándar: $!standardNotice.standardRecipient

Template destino: $!standardNotice.targetTemplateType
Template destino encontrado: $!standardNotice.targetTemplateFound
Destinatario destino: $!standardNotice.targetRecipient
Idioma destino: $!standardNotice.targetLanguage
Sender destino: $!standardNotice.targetSender

Asunto enviado:
$!standardNotice.targetSubject','Se registró un envío de correo a un tercero.

Elección: $!standardNotice.electionId#if($election.titleSpanish) - $!election.titleSpanish#end
Nominación: $!standardNotice.nominationId#if($nomination.nominationName) - $!nomination.nominationName#end
Candidato: $!standardNotice.candidateId#if($candidate.name) - $!candidate.name#end
Auditor: $!standardNotice.auditorId#if($auditor.name) - $!auditor.name#end
#if($organization.id || $organization.name)
Organización: $!organization.id#if($organization.name) - $!organization.name#end
#end
#if($standardNotice.supportNominationId || $supportingOrganization.name || $supportNomination.supportingContactName)
Apoyo: $!standardNotice.supportNominationId#if($supportingOrganization.name) - $!supportingOrganization.name#end#if($supportNomination.supportingContactName) (Contacto: $!supportNomination.supportingContactName)#end
#end

Evento: $!standardNotice.eventType
Fecha: $!standardNotice.createdAt

Template estándar: $!standardNotice.standardTemplateType
Destinatario estándar: $!standardNotice.standardRecipient

Template destino: $!standardNotice.targetTemplateType
Template destino encontrado: $!standardNotice.targetTemplateFound
Destinatario destino: $!standardNotice.targetRecipient
Idioma destino: $!standardNotice.targetLanguage
Sender destino: $!standardNotice.targetSender

Asunto enviado:
$!standardNotice.targetSubject','STANDARD_DISPATCH_NOTICE',NULL),
	 (3411,'[Elecciones] Recordatorio de nominación pendiente - $election.titleSpanish','[Elecciones] Recordatorio de nominación pendiente - $election.titleSpanish','[Elecciones] Recordatorio de nominación pendiente - $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $nomination.nominationName,

Le recordamos que tiene una nominación pendiente para la elección:

$election.titleSpanish

#if($organization.name)
Organización nominadora: $organization.name
#end

Para continuar y completar su nominación, ingrese al siguiente enlace:

$nomination.acceptNominationLink

Si ya completó este proceso, por favor ignore este mensaje.

--
Dear $nomination.nominationName,

This is a reminder that you have a pending nomination for the election:

$election.titleEnglish

#if($organization.name)
Nominating organization: $organization.name
#end

To continue and complete your nomination, please use the following link:

$nomination.acceptNominationLink

If you have already completed this process, please ignore this message.

--

Prezado(a) $nomination.nominationName,

Lembramos que você tem uma nomeação pendente para a eleição:

$election.titlePortuguese

#if($organization.name)
Organização nomeadora: $organization.name
#end

Para continuar e completar sua nomeação, acesse o seguinte link:

$nomination.acceptNominationLink

Se você já concluiu este processo, por favor ignore esta mensagem.','[English below]
[Português abaixo]

Estimado/a $nomination.nominationName,

Le recordamos que tiene una nominación pendiente para la elección:

$election.titleSpanish

#if($organization.name)
Organización nominadora: $organization.name
#end

Para continuar y completar su nominación, ingrese al siguiente enlace:

$nomination.acceptNominationLink

Si ya completó este proceso, por favor ignore este mensaje.

--
Dear $nomination.nominationName,

This is a reminder that you have a pending nomination for the election:

$election.titleEnglish

#if($organization.name)
Nominating organization: $organization.name
#end

To continue and complete your nomination, please use the following link:

$nomination.acceptNominationLink

If you have already completed this process, please ignore this message.

--

Prezado(a) $nomination.nominationName,

Lembramos que você tem uma nomeação pendente para a eleição:

$election.titlePortuguese

#if($organization.name)
Organização nomeadora: $organization.name
#end

Para continuar e completar sua nomeação, acesse o seguinte link:

$nomination.acceptNominationLink

Se você já concluiu este processo, por favor ignore esta mensagem.','[English below]
[Português abaixo]

Estimado/a $nomination.nominationName,

Le recordamos que tiene una nominación pendiente para la elección:

$election.titleSpanish

#if($organization.name)
Organización nominadora: $organization.name
#end

Para continuar y completar su nominación, ingrese al siguiente enlace:

$nomination.acceptNominationLink

Si ya completó este proceso, por favor ignore este mensaje.

--
Dear $nomination.nominationName,

This is a reminder that you have a pending nomination for the election:

$election.titleEnglish

#if($organization.name)
Nominating organization: $organization.name
#end

To continue and complete your nomination, please use the following link:

$nomination.acceptNominationLink

If you have already completed this process, please ignore this message.

--

Prezado(a) $nomination.nominationName,

Lembramos que você tem uma nomeação pendente para a eleição:

$election.titlePortuguese

#if($organization.name)
Organização nomeadora: $organization.name
#end

Para continuar e completar sua nomeação, acesse o seguinte link:

$nomination.acceptNominationLink

Se você já concluiu este processo, por favor ignore esta mensagem.','NOMINATION_REMINDER',NULL),
	 (3547,'la organización','la organización','la organización','la organización
$email
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','la organización
$email
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','la organización
$email
Casa de Internet de Latinoamérica y el Caribe
Rambla Rep. de México 6125
11400 Montevideo-Uruguay
+598 2604 22 22
www.example.org','SIGNATURE',NULL);
INSERT INTO electionemailtemplate (electionemailtemplate_id,subjecten,subjectsp,subjectpt,bodyen,bodysp,bodypt,"type",election_id) VALUES
	 (3648,'[Elections] Your organization ($organization.orgId) can carry out the nomination of candidates - $election.titleEnglish','[Elecciones] Su organización ($organization.orgId) puede realizar la nominación de candidatos - $election.titleSpanish','[Eleições] Sua organização ($organization.orgId) pode realizar a nomeação de candidatos - $election.titlePortuguese','Dear $organization.membershipContactName,

Your organization can carry out the nomination of candidates for the election:

$election.titleEnglish

Organization details:
Name: $organization.name
Organization ID: $organization.orgId
#if($organization.category)Category: $organization.category#end
#if($organization.country)Country: $organization.country#end
#if($organization.membershipContactEmail)Membership contact email: $organization.membershipContactEmail#end

To submit the nomination of candidates, please use the following link:

$organization.doNominationLink

If your organization has already submitted the nomination of candidates, please ignore this message.

$signature','Estimado/a $organization.membershipContactName,

Le informamos que su organización puede realizar la nominación de candidatos para la elección:

$election.titleSpanish

Datos de la organización:
Nombre: $organization.name
ID de organización: $organization.orgId
#if($organization.category)Categoría: $organization.category#end
#if($organization.country)País: $organization.country#end
#if($organization.membershipContactEmail)Correo de contacto de membresía: $organization.membershipContactEmail#end

Para registrar la nominación de candidatos, por favor ingrese al siguiente enlace:

$organization.doNominationLink

Si su organización ya realizó la nominación de candidatos, por favor ignore este mensaje.

$signature','Prezado(a) $organization.membershipContactName,

Informamos que sua organização pode realizar a nomeação de candidatos para a eleição:

$election.titlePortuguese

Dados da organização:
Nome: $organization.name
ID da organização: $organization.orgId
#if($organization.category)Categoria: $organization.category#end
#if($organization.country)País: $organization.country#end
#if($organization.membershipContactEmail)E-mail de contato de membresia: $organization.membershipContactEmail#end

Para registrar a nomeação de candidatos, acesse o seguinte link:

$organization.doNominationLink

Se sua organização já realizou a nomeação de candidatos, por favor ignore esta mensagem.

$signature','NOMINATION_ORG_CANDIDATES_INVITATION',NULL),
	 (3649,'[Elections] Your organization ($organization.orgId) can carry out the nomination of candidates - $election.titleEnglish','[Elecciones] Su organización ($organization.orgId) puede realizar la nominación de candidatos - $election.titleSpanish','[Eleições] Sua organização ($organization.orgId) pode realizar a nomeação de candidatos - $election.titlePortuguese','Dear $organization.membershipContactName,

Your organization can carry out the nomination of candidates for the election:

$election.titleEnglish

Organization details:
Name: $organization.name
Organization ID: $organization.orgId
#if($organization.category)Category: $organization.category#end
#if($organization.country)Country: $organization.country#end
#if($organization.membershipContactEmail)Membership contact email: $organization.membershipContactEmail#end

To submit the nomination of candidates, please use the following link:

$organization.doNominationLink

If your organization has already submitted the nomination of candidates, please ignore this message.

In non-statutory elections, membership contacts may only nominate candidates. Voting rights are restricted to members of the policy-list@example.org mailing list who have sufficient seniority, as established in applicable policies. To participate in future elections, it is necessary to subscribe to the list at: https://example.org/policies/list

$signature','Estimado/a $organization.membershipContactName,

Le informamos que su organización puede realizar la nominación de candidatos para la elección:

$election.titleSpanish

Datos de la organización:
Nombre: $organization.name
ID de organización: $organization.orgId
#if($organization.category)Categoría: $organization.category#end
#if($organization.country)País: $organization.country#end
#if($organization.membershipContactEmail)Correo de contacto de membresía: $organization.membershipContactEmail#end

Para registrar la nominación de candidatos, por favor ingrese al siguiente enlace:

$organization.doNominationLink

Si su organización ya realizó la nominación de candidatos, por favor ignore este mensaje.

En elecciones No estatutarias, los contactos de membresía de la organización únicamente pueden nominar candidatos. El derecho a voto está restringido a los integrantes de la lista policy-list@example.org que cuenten con la antigüedad suficiente según lo establecido en las políticas de la organización. Para participar en futuras elecciones, es necesario suscribirse a la lista en: https://example.org/policies/list

$signature','Prezado(a) $organization.membershipContactName,

Informamos que sua organização pode realizar a nomeação de candidatos para a eleição:

$election.titlePortuguese

Dados da organização:
Nome: $organization.name
ID da organização: $organization.orgId
#if($organization.category)Categoria: $organization.category#end
#if($organization.country)País: $organization.country#end
#if($organization.membershipContactEmail)E-mail de contato de membresia: $organization.membershipContactEmail#end

Para registrar a nomeação de candidatos, acesse o seguinte link:

$organization.doNominationLink

Se sua organização já realizou a nomeação de candidatos, por favor ignore esta mensagem.

Em eleições não estatutárias, os contatos de membresia da organização podem apenas nomear candidatos. O direito a voto é restrito aos integrantes da lista policy-list@example.org que tenham a antiguidade suficiente, conforme estabelecido nas políticas da organização. Para participar de futuras eleições, é necessário inscrever-se na lista em: https://example.org/policies/list

$signature','NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA',NULL),
	(3776,'[Elections] Evaluation access requested - $election.titleEnglish - $candidate.name','[Elecciones] Solicitud de acceso a la evaluación - $election.titleSpanish - $candidate.name','[Eleições] Solicitação de acesso à avaliação - $election.titlePortuguese - $candidate.name','Dear team,

Candidate $candidate.name has requested access to the evaluation in the election:

$election.titleEnglish

Details:
Candidate ID: $candidate.candidateId
Email: $candidate.mail
Requested task: $requestedTaskKey

Pending tasks:
$candidatePendingTasks

Completed tasks:
$candidateCompletedTasks

$signature','Estimados,

El candidato $candidate.name ha solicitado acceso a la evaluación en la elección:

$election.titleSpanish

Detalle:
Candidato ID: $candidate.candidateId
Correo: $candidate.mail
Tarea solicitada: $requestedTaskKey

Tareas pendientes:
$candidatePendingTasks

Tareas completadas:
$candidateCompletedTasks

$signature','Prezados,

O(a) candidato(a) $candidate.name solicitou acesso à avaliação na eleição:

$election.titlePortuguese

Detalhes:
ID do candidato: $candidate.candidateId
Email: $candidate.mail
Tarefa solicitada: $requestedTaskKey

Tarefas pendentes:
$candidatePendingTasks

Tarefas concluídas:
$candidateCompletedTasks

$signature','CANDIDATE_EVALUATION_ACCESS_REQUESTED_ADMIN',NULL),
	 (4216,'[Elecciones][Elections][Eleições] Recordatorio de apoyo pendiente / Pending support reminder / Lembrete de apoio pendente - $election.titleSpanish','[Elecciones][Elections][Eleições] Recordatorio de apoyo pendiente / Pending support reminder / Lembrete de apoio pendente - $election.titleSpanish','[Elecciones][Elections][Eleições] Recordatorio de apoyo pendiente / Pending support reminder / Lembrete de apoio pendente - $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Le recordamos que la solicitud de apoyo a su candidatura enviada a $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) aún no ha sido respondida.

Elección: $election.titleSpanish

Puede contactar directamente a la persona/organización para dar seguimiento.
Si ya fue respondida, por favor ignore este mensaje.

--
Dear $candidate.name,

This is a reminder that the support request for your candidacy sent to $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) has not been answered yet.

Election: $election.titleEnglish

You may contact the person/organization directly to follow up.
If it has already been answered, please ignore this message.

--
Prezado(a) $candidate.name,

Lembramos que a solicitação de apoio à sua candidatura enviada para $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) ainda não foi respondida.

Eleição: $election.titlePortuguese

Você pode contatar diretamente a pessoa/organização para acompanhar.
Se já foi respondida, por favor ignore esta mensagem.

$signature','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Le recordamos que la solicitud de apoyo a su candidatura enviada a $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) aún no ha sido respondida.

Elección: $election.titleSpanish

Puede contactar directamente a la persona/organización para dar seguimiento.
Si ya fue respondida, por favor ignore este mensaje.

--
Dear $candidate.name,

This is a reminder that the support request for your candidacy sent to $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) has not been answered yet.

Election: $election.titleEnglish

You may contact the person/organization directly to follow up.
If it has already been answered, please ignore this message.

--
Prezado(a) $candidate.name,

Lembramos que a solicitação de apoio à sua candidatura enviada para $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) ainda não foi respondida.

Eleição: $election.titlePortuguese

Você pode contatar diretamente a pessoa/organização para acompanhar.
Se já foi respondida, por favor ignore esta mensagem.

$signature','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Le recordamos que la solicitud de apoyo a su candidatura enviada a $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) aún no ha sido respondida.

Elección: $election.titleSpanish

Puede contactar directamente a la persona/organización para dar seguimiento.
Si ya fue respondida, por favor ignore este mensaje.

--
Dear $candidate.name,

This is a reminder that the support request for your candidacy sent to $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) has not been answered yet.

Election: $election.titleEnglish

You may contact the person/organization directly to follow up.
If it has already been answered, please ignore this message.

--
Prezado(a) $candidate.name,

Lembramos que a solicitação de apoio à sua candidatura enviada para $supportNomination.supportingContactName ($supportNomination.supportingContactEmail) ainda não foi respondida.

Eleição: $election.titlePortuguese

Você pode contatar diretamente a pessoa/organização para acompanhar.
Se já foi respondida, por favor ignore esta mensagem.

$signature','SUPPORT_REQUESTER_REMINDER',NULL),
	 (3880,'[Elecciones][Elections][Eleições] Candidatura aprobada y publicada / Candidacy approved and published / Candidatura aprovada e publicada','[Elecciones][Elections][Eleições] Candidatura aprobada y publicada / Candidacy approved and published / Candidatura aprovada e publicada','[Elecciones][Elections][Eleições] Candidatura aprobada y publicada / Candidacy approved and published / Candidatura aprovada e publicada','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Nos complace informarle que su candidatura para la elección:

$election.titleSpanish

ha sido aprobada y publicada.

Su perfil ya forma parte de la lista oficial de candidatos/as.

Si tiene consultas, puede responder a este correo.

--

Dear $candidate.name,

We are pleased to inform you that your candidacy for the election:

$election.titleEnglish

has been approved and published.

Your profile is now part of the official list of candidates.

If you have any questions, you may reply to this email.

--

Prezado(a) $candidate.name,

Temos o prazer de informar que sua candidatura para a eleição:

$election.titlePortuguese

foi aprovada e publicada.

Seu perfil agora faz parte da lista oficial de candidatos(as).

Se tiver dúvidas, você pode responder a este e-mail.

$signature','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Nos complace informarle que su candidatura para la elección:

$election.titleSpanish

ha sido aprobada y publicada.

Su perfil ya forma parte de la lista oficial de candidatos/as.

Si tiene consultas, puede responder a este correo.

--

Dear $candidate.name,

We are pleased to inform you that your candidacy for the election:

$election.titleEnglish

has been approved and published.

Your profile is now part of the official list of candidates.

If you have any questions, you may reply to this email.

--

Prezado(a) $candidate.name,

Temos o prazer de informar que sua candidatura para a eleição:

$election.titlePortuguese

foi aprovada e publicada.

Seu perfil agora faz parte da lista oficial de candidatos(as).

Se tiver dúvidas, você pode responder a este e-mail.

$signature','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Nos complace informarle que su candidatura para la elección:

$election.titleSpanish

ha sido aprobada y publicada.

Su perfil ya forma parte de la lista oficial de candidatos/as.

Si tiene consultas, puede responder a este correo.

--

Dear $candidate.name,

We are pleased to inform you that your candidacy for the election:

$election.titleEnglish

has been approved and published.

Your profile is now part of the official list of candidates.

If you have any questions, you may reply to this email.

--

Prezado(a) $candidate.name,

Temos o prazer de informar que sua candidatura para a eleição:

$election.titlePortuguese

foi aprovada e publicada.

Seu perfil agora faz parte da lista oficial de candidatos(as).

Se tiver dúvidas, você pode responder a este e-mail.

$signature','CANDIDATE_CONFIRMED_AND_PUBLISHED',NULL),
	 (3950,'[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[Elecciones][Elections][Eleições] $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $user.name,

En este momento está activo su enlace para participar en
$election.titleSpanish

A partir de ahora usted podrá acceder al siguiente enlace y votar:
$user.voteLink

--

Dear $user.name,

From this moment on your link is enabled to participate in
$election.titleEnglish

You can now access it below and vote:
$user.voteLink

--

Prezado(a) $user.name,

A partir deste momento está ativado o seu link para participar na
$election.titlePortuguese

Você já pode acessá-lo abaixo e votar:
$user.voteLink

$signature','[English below]
[Português abaixo]

Estimado/a $user.name,

En este momento está activo su enlace para participar en
$election.titleSpanish

A partir de ahora usted podrá acceder al siguiente enlace y votar:
$user.voteLink

--

Dear $user.name,

From this moment on your link is enabled to participate in
$election.titleEnglish

You can now access it below and vote:
$user.voteLink

--

Prezado(a) $user.name,

A partir deste momento está ativado o seu link para participar na
$election.titlePortuguese

Você já pode acessá-lo abaixo e votar:
$user.voteLink

$signature','[English below]
[Português abaixo]

Estimado/a $user.name,

En este momento está activo su enlace para participar en
$election.titleSpanish

A partir de ahora usted podrá acceder al siguiente enlace y votar:
$user.voteLink

--

Dear $user.name,

From this moment on your link is enabled to participate in
$election.titleEnglish

You can now access it below and vote:
$user.voteLink

--

Prezado(a) $user.name,

A partir deste momento está ativado o seu link para participar na
$election.titlePortuguese

Você já pode acessá-lo abaixo e votar:
$user.voteLink

$signature','ELECTION_START_SP_PT_EN',NULL),
	 (3412,'[Elections] Candidacy support request reminder - $election.titleEnglish','[Elecciones] Recordatorio de solicitud de apoyo a candidatura - $election.titleSpanish','[Eleições] Lembrete de solicitação de apoio à candidatura - $election.titlePortuguese','Dear #if($supportNomination.supportingContactName)$supportNomination.supportingContactName#elseif($supportingOrganization.membershipContactName)$supportingOrganization.membershipContactName#elsesupport contact#end,

This is a reminder that you have a pending support request for a candidacy in the election:

$election.titleEnglish

Nominee details:
Name: $nomination.nominationName
Email: $nomination.nominationEmail
#if($supportingOrganization.name)
Organization requested to provide support: $supportingOrganization.name
#end
#if($nomination.nominationReasonEnglish)
Nominated by:
$nomination.nominationReasonEnglish
#end

To submit your decision regarding this support request, please use the following link:

$supportNomination.supportNominationLink

If you have already responded to this request, please ignore this message.

$signature','Estimado/a #if($supportNomination.supportingContactName)$supportNomination.supportingContactName#elseif($supportingOrganization.membershipContactName)$supportingOrganization.membershipContactName#elsecontacto de apoyo#end,

Le recordamos que tiene una solicitud de apoyo pendiente para una candidatura en la elección:

$election.titleSpanish

Datos del aspirante:
Nombre: $nomination.nominationName
Correo: $nomination.nominationEmail
#if($supportingOrganization.name)
Organización a la que se solicita apoyo: $supportingOrganization.name
#end
#if($nomination.nominationReasonSpanish)
Nominado por:
$nomination.nominationReasonSpanish
#end

Para registrar su decisión sobre esta solicitud de apoyo, por favor ingrese al siguiente enlace:

$supportNomination.supportNominationLink

Si ya respondió esta solicitud, por favor ignore este mensaje.

$signature','Prezado(a) #if($supportNomination.supportingContactName)$supportNomination.supportingContactName#elseif($supportingOrganization.membershipContactName)$supportingOrganization.membershipContactName#elsecontato de apoio#end,

Lembramos que você tem uma solicitação de apoio pendente para uma candidatura na eleição:

$election.titlePortuguese

Dados do(a) aspirante:
Nome: $nomination.nominationName
E-mail: $nomination.nominationEmail
#if($supportingOrganization.name)
Organização à qual se solicita apoio: $supportingOrganization.name
#end
#if($nomination.nominationReasonPortuguese)
Indicado por:
$nomination.nominationReasonPortuguese
#end

Para registrar sua decisão sobre esta solicitação de apoio, acesse o seguinte link:

$supportNomination.supportNominationLink

Se você já respondeu esta solicitação, por favor ignore esta mensagem.

$signature','SUPPORT_REMINDER',NULL),
	 (4072,'[Elecciones][Elections][Eleições] Inscripción al curso / Course enrollment / Inscrição no curso - $election.titleSpanish','[Elecciones][Elections][Eleições] Inscripción al curso / Course enrollment / Inscrição no curso - $election.titleSpanish','[Elecciones][Elections][Eleições] Inscripción al curso / Course enrollment / Inscrição no curso - $election.titleSpanish','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Le informamos que su inscripción al curso de capacitación correspondiente a la elección:

$election.titleSpanish

fue realizada correctamente.

Le recomendamos ingresar al campus y revisar el contenido del curso a la brevedad, para avanzar con su postulación sin demoras:

https://campus.example.org/

Si es la primera vez que accede al Campus, en breve recibirá un correo electrónico adicional con sus credenciales de acceso a la plataforma. Por favor, verifique también su carpeta de spam o correo no deseado.

Si tiene dudas o necesita ayuda, puede responder a este correo.

--

Dear $candidate.name,

We inform you that your enrollment in the training course for the election:

$election.titleEnglish

has been completed successfully.

We recommend that you log in to the campus and review the course content as soon as possible to continue your application without delays:

https://campus.example.org/

If this is your first time accessing Campus, you will soon receive an additional email with your platform access credentials. Please also check your spam or junk mail folder.

If you have questions or need assistance, you may reply to this email.

--

Prezado(a) $candidate.name,

Informamos que sua inscrição no curso de capacitação correspondente à eleição:

$election.titlePortuguese

foi realizada com sucesso.

Recomendamos que você acesse o campus e revise o conteúdo do curso o quanto antes, para avançar com sua candidatura sem atrasos:

https://campus.example.org/

Se esta for a sua primeira vez acessando o Campus, em breve você receberá um e-mail adicional com suas credenciais de acesso à plataforma. Por favor, verifique também a pasta de spam ou lixo eletrônico.

Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail.

$signature','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Le informamos que su inscripción al curso de capacitación correspondiente a la elección:

$election.titleSpanish

fue realizada correctamente.

Le recomendamos ingresar al campus y revisar el contenido del curso a la brevedad, para avanzar con su postulación sin demoras:

https://campus.example.org/

Si es la primera vez que accede al Campus, en breve recibirá un correo electrónico adicional con sus credenciales de acceso a la plataforma. Por favor, verifique también su carpeta de spam o correo no deseado.

Si tiene dudas o necesita ayuda, puede responder a este correo.

--

Dear $candidate.name,

We inform you that your enrollment in the training course for the election:

$election.titleEnglish

has been completed successfully.

We recommend that you log in to the campus and review the course content as soon as possible to continue your application without delays:

https://campus.example.org/

If this is your first time accessing Campus, you will soon receive an additional email with your platform access credentials. Please also check your spam or junk mail folder.

If you have questions or need assistance, you may reply to this email.

--

Prezado(a) $candidate.name,

Informamos que sua inscrição no curso de capacitação correspondente à eleição:

$election.titlePortuguese

foi realizada com sucesso.

Recomendamos que você acesse o campus e revise o conteúdo do curso o quanto antes, para avançar com sua candidatura sem atrasos:

https://campus.example.org/

Se esta for a sua primeira vez acessando o Campus, em breve você receberá um e-mail adicional com suas credenciais de acesso à plataforma. Por favor, verifique também a pasta de spam ou lixo eletrônico.

Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail.

$signature','[English below]
[Português abaixo]

Estimado/a $candidate.name,

Le informamos que su inscripción al curso de capacitación correspondiente a la elección:

$election.titleSpanish

fue realizada correctamente.

Le recomendamos ingresar al campus y revisar el contenido del curso a la brevedad, para avanzar con su postulación sin demoras:

https://campus.example.org/

Si es la primera vez que accede al Campus, en breve recibirá un correo electrónico adicional con sus credenciales de acceso a la plataforma. Por favor, verifique también su carpeta de spam o correo no deseado.

Si tiene dudas o necesita ayuda, puede responder a este correo.

--

Dear $candidate.name,

We inform you that your enrollment in the training course for the election:

$election.titleEnglish

has been completed successfully.

We recommend that you log in to the campus and review the course content as soon as possible to continue your application without delays:

https://campus.example.org/

If this is your first time accessing Campus, you will soon receive an additional email with your platform access credentials. Please also check your spam or junk mail folder.

If you have questions or need assistance, you may reply to this email.

--

Prezado(a) $candidate.name,

Informamos que sua inscrição no curso de capacitação correspondente à eleição:

$election.titlePortuguese

foi realizada com sucesso.

Recomendamos que você acesse o campus e revise o conteúdo do curso o quanto antes, para avançar com sua candidatura sem atrasos:

https://campus.example.org/

Se esta for a sua primeira vez acessando o Campus, em breve você receberá um e-mail adicional com suas credenciais de acesso à plataforma. Por favor, verifique também a pasta de spam ou lixo eletrônico.

Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail.

$signature','CANDIDATE_TRAINING_ACCESS_REQUESTED_ADMIN',NULL),
	 (4288,'[Elections] Synchronization error - $election.titleSpanish','[Elecciones] Error en sincronización - $election.titleSpanish','[Eleições] Erro na sincronização - $election.titleSpanish','A synchronization error was detected for $syncTypeLabel.

Election: $election.electionId - $election.titleSpanish
Date/Time: $syncTimestamp
Status: $syncStatus
Processed: $processedRows
Created: $createdRows
Updated: $updatedRows
Deleted: $deletedRows
Detail: $syncMessage
Sync Run Id: $syncRunId','Se detectó un error en la sincronización de $syncTypeLabel.

Elección: $election.electionId - $election.titleSpanish
Fecha/Hora: $syncTimestamp
Estado: $syncStatus
Procesadas: $processedRows
Creadas: $createdRows
Actualizadas: $updatedRows
Eliminadas: $deletedRows
Detalle: $syncMessage
Sync Run Id: $syncRunId','Foi detectado um erro na sincronização de $syncTypeLabel.

Eleição: $election.electionId - $election.titleSpanish
Data/Hora: $syncTimestamp
Estado: $syncStatus
Processadas: $processedRows
Criadas: $createdRows
Atualizadas: $updatedRows
Eliminadas: $deletedRows
Detalhe: $syncMessage
Sync Run Id: $syncRunId','SYNC_ERROR_ALERT',NULL),
	 (4289,'[Elecciones] [Elections] [Eleições] [$candidateQuestionId] [$candidate.candidateId] [$candidateQuestionNewStatus] Pregunta al candidato / Candidate question / Pergunta ao candidato','[Elecciones] [Elections] [Eleições] [$candidateQuestionId] [$candidate.candidateId] [$candidateQuestionNewStatus] Pregunta al candidato / Candidate question / Pergunta ao candidato','[Elecciones] [Elections] [Eleições] [$candidateQuestionId] [$candidate.candidateId] [$candidateQuestionNewStatus] Pregunta al candidato / Candidate question / Pergunta ao candidato','[English below]
[Português abaixo]

Estimado/a,

Se actualizó una pregunta de la comunidad en esta elección:
$election.titleSpanish

Acción requerida:
$candidateQuestionActionRequired

Estado anterior: $candidateQuestionPreviousStatus
Estado actual: $candidateQuestionNewStatus

Pregunta:
$candidateQuestionText

Respuesta:
$candidateQuestionAnswer

Fecha: $candidateQuestionEventDateUtc

Candidato: $candidateQuestionCandidateName
Realizada por: $candidateQuestionAskedByInitialName

--
Dear recipient,

A community question in this election was updated:
$election.titleEnglish

Action required:
$candidateQuestionActionRequired

Previous status: $candidateQuestionPreviousStatus
Current status: $candidateQuestionNewStatus

Question:
$candidateQuestionText

Answer:
$candidateQuestionAnswer

Date: $candidateQuestionEventDateUtc

Candidate: $candidateQuestionCandidateName
Asked by: $candidateQuestionAskedByInitialName

--
Prezado(a),

Uma pergunta da comunidade nesta eleição foi atualizada:
$election.titlePortuguese

Ação requerida:
$candidateQuestionActionRequired

Status anterior: $candidateQuestionPreviousStatus
Status atual: $candidateQuestionNewStatus

Pergunta:
$candidateQuestionText

Resposta:
$candidateQuestionAnswer

Data: $candidateQuestionEventDateUtc

Candidato: $candidateQuestionCandidateName
Feita por: $candidateQuestionAskedByInitialName

$signature','[English below]
[Português abaixo]

Estimado/a,

Se actualizó una pregunta de la comunidad en esta elección:
$election.titleSpanish

Acción requerida:
$candidateQuestionActionRequired

Estado anterior: $candidateQuestionPreviousStatus
Estado actual: $candidateQuestionNewStatus

Pregunta:
$candidateQuestionText

Respuesta:
$candidateQuestionAnswer

Fecha: $candidateQuestionEventDateUtc

Candidato: $candidateQuestionCandidateName
Realizada por: $candidateQuestionAskedByInitialName

--
Dear recipient,

A community question in this election was updated:
$election.titleEnglish

Action required:
$candidateQuestionActionRequired

Previous status: $candidateQuestionPreviousStatus
Current status: $candidateQuestionNewStatus

Question:
$candidateQuestionText

Answer:
$candidateQuestionAnswer

Date: $candidateQuestionEventDateUtc

Candidate: $candidateQuestionCandidateName
Asked by: $candidateQuestionAskedByInitialName

--
Prezado(a),

Uma pergunta da comunidade nesta eleição foi atualizada:
$election.titlePortuguese

Ação requerida:
$candidateQuestionActionRequired

Status anterior: $candidateQuestionPreviousStatus
Status atual: $candidateQuestionNewStatus

Pergunta:
$candidateQuestionText

Resposta:
$candidateQuestionAnswer

Data: $candidateQuestionEventDateUtc

Candidato: $candidateQuestionCandidateName
Feita por: $candidateQuestionAskedByInitialName

$signature','[English below]
[Português abaixo]

Estimado/a,

Se actualizó una pregunta de la comunidad en esta elección:
$election.titleSpanish

Acción requerida:
$candidateQuestionActionRequired

Estado anterior: $candidateQuestionPreviousStatus
Estado actual: $candidateQuestionNewStatus

Pregunta:
$candidateQuestionText

Respuesta:
$candidateQuestionAnswer

Fecha: $candidateQuestionEventDateUtc

Candidato: $candidateQuestionCandidateName
Realizada por: $candidateQuestionAskedByInitialName

--
Dear recipient,

A community question in this election was updated:
$election.titleEnglish

Action required:
$candidateQuestionActionRequired

Previous status: $candidateQuestionPreviousStatus
Current status: $candidateQuestionNewStatus

Question:
$candidateQuestionText

Answer:
$candidateQuestionAnswer

Date: $candidateQuestionEventDateUtc

Candidate: $candidateQuestionCandidateName
Asked by: $candidateQuestionAskedByInitialName

--
Prezado(a),

Uma pergunta da comunidade nesta eleição foi atualizada:
$election.titlePortuguese

Ação requerida:
$candidateQuestionActionRequired

Status anterior: $candidateQuestionPreviousStatus
Status atual: $candidateQuestionNewStatus

Pergunta:
$candidateQuestionText

Resposta:
$candidateQuestionAnswer

Data: $candidateQuestionEventDateUtc

Candidato: $candidateQuestionCandidateName
Feita por: $candidateQuestionAskedByInitialName

$signature','CANDIDATE_QUESTION_STATUS_NOTIFICATION',NULL),
	 (4290,'[Elections] Link recovery / Recuperacion de enlace / Recuperacao de link','[Elections] Link recovery / Recuperacion de enlace / Recuperacao de link','[Elections] Link recovery / Recuperacion de enlace / Recuperacao de link','[English below]
[Português abaixo]

Estimado/a,

Recibimos una solicitud para recuperar un enlace de acceso correspondiente a la elección:

$election.titleSpanish

Tipo de enlace: $linkRecoveryTypeSpanish
Link: $linkRecoveryUrl

Si usted no solicitó esta recuperación, puede ignorar este mensaje.

Si tiene dudas o necesita ayuda, puede responder a este correo.

--

Dear recipient,

We received a request to recover an access link for the election:

$election.titleEnglish

Link type: $linkRecoveryTypeEnglish
Link: $linkRecoveryUrl

If you did not request this recovery, you may ignore this message.

If you have questions or need assistance, you may reply to this email.

--

Prezado(a),

Recebemos uma solicitação para recuperar um link de acesso correspondente à eleição:

$election.titlePortuguese

Tipo de link: $linkRecoveryTypePortuguese
Link: $linkRecoveryUrl

Se você não solicitó esta recuperação, pode ignorar esta mensagem.

Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail.

$signature','[English below]
[Português abaixo]

Estimado/a,

Recibimos una solicitud para recuperar un enlace de acceso correspondiente a la elección:

$election.titleSpanish

Tipo de enlace: $linkRecoveryTypeSpanish
Link: $linkRecoveryUrl

Si usted no solicitó esta recuperación, puede ignorar este mensaje.

Si tiene dudas o necesita ayuda, puede responder a este correo.

--

Dear recipient,

We received a request to recover an access link for the election:

$election.titleEnglish

Link type: $linkRecoveryTypeEnglish
Link: $linkRecoveryUrl

If you did not request this recovery, you may ignore this message.

If you have questions or need assistance, you may reply to this email.

--

Prezado(a),

Recebemos uma solicitação para recuperar um link de acesso correspondente à eleição:

$election.titlePortuguese

Tipo de link: $linkRecoveryTypePortuguese
Link: $linkRecoveryUrl

Se você não solicitó esta recuperação, pode ignorar esta mensagem.

Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail.

$signature','[English below]
[Português abaixo]

Estimado/a,

Recibimos una solicitud para recuperar un enlace de acceso correspondiente a la elección:

$election.titleSpanish

Tipo de enlace: $linkRecoveryTypeSpanish
Link: $linkRecoveryUrl

Si usted no solicitó esta recuperación, puede ignorar este mensaje.

Si tiene dudas o necesita ayuda, puede responder a este correo.

--

Dear recipient,

We received a request to recover an access link for the election:

$election.titleEnglish

Link type: $linkRecoveryTypeEnglish
Link: $linkRecoveryUrl

If you did not request this recovery, you may ignore this message.

If you have questions or need assistance, you may reply to this email.

--

Prezado(a),

Recebemos uma solicitação para recuperar um link de acesso correspondente à eleição:

$election.titlePortuguese

Tipo de link: $linkRecoveryTypePortuguese
Link: $linkRecoveryUrl

Se você não solicitó esta recuperação, pode ignorar esta mensagem.

Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail.

$signature','LINK_RECOVERY',NULL);
