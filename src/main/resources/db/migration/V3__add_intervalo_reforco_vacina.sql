ALTER TABLE VACINAS ADD vacina_intervalo_reforco_dias INTEGER;

UPDATE VACINAS SET vacina_intervalo_reforco_dias = 365
 WHERE vacina_nome IN ('V10 (Polivalente)', 'Antirrabica', 'Triplice Felina');

UPDATE VACINAS SET vacina_intervalo_reforco_dias = 21
 WHERE vacina_nome = 'Giardia';
