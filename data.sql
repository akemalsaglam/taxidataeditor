-- Drop table

-- DROP TABLE public.bursa_taxi_data_month1;

CREATE TABLE public.bursa_taxi_data_month1 (
	id int8 NOT NULL,
	taxi_id int8 NULL,
	"date" timestamp NULL,
	speed int8 NULL,
	length numeric NULL,
	direction varchar NULL,
	"position" varchar NULL,
	line varchar NULL,
	CONSTRAINT bursa_taxi_data_month1_pk PRIMARY KEY (id)
);
CREATE INDEX month1_id_index ON public.bursa_taxi_data_month1 USING btree (id);
CREATE INDEX month1_taxi_id_index ON public.bursa_taxi_data_month1 USING btree (taxi_id);
