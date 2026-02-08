--
-- PostgreSQL database dump
--

\restrict TfNa5Sy9ixDAuySnGMkouVj2QaHxZvYC3FnK2vUDFJwJBckZfQTyJHtEKP4VCBE

-- Dumped from database version 15.4 (Debian 15.4-1.pgdg110+1)
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: member_stat_thief; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.member_stat_thief (
    average_survival_sec integer,
    is_deleted boolean,
    longest_survival_sec integer,
    total_mission_count integer,
    created_at timestamp(6) with time zone NOT NULL,
    grade_thief_id bigint,
    member_id bigint NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.member_stat_thief OWNER TO root;

--
-- Data for Name: member_stat_thief; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.member_stat_thief (average_survival_sec, is_deleted, longest_survival_sec, total_mission_count, created_at, grade_thief_id, member_id, updated_at) FROM stdin;
0	f	0	0	2026-01-30 20:45:36.065043+09	1	5	2026-01-30 20:45:36.065043+09
0	f	0	1	2026-01-30 20:45:09.030248+09	8	1	2026-02-05 20:52:32.906344+09
0	f	0	0	2026-01-31 20:07:45.638957+09	1	7	2026-01-31 20:07:45.638957+09
0	f	0	0	2026-01-31 20:08:43.907837+09	1	8	2026-01-31 20:08:43.907837+09
0	f	0	0	2026-01-30 23:06:28.106802+09	2	6	2026-01-31 20:34:31.430203+09
0	f	0	0	2026-01-30 20:45:15.59166+09	11	2	2026-02-02 01:16:15.712421+09
0	f	0	0	2026-01-30 20:45:22.512539+09	11	3	2026-02-02 01:16:15.712455+09
0	f	0	0	2026-01-30 20:45:29.943661+09	11	4	2026-02-02 01:16:15.712498+09
\.


--
-- Name: member_stat_thief member_stat_thief_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat_thief
    ADD CONSTRAINT member_stat_thief_pkey PRIMARY KEY (member_id);


--
-- Name: member_stat_thief fkb50y4523m1it0tvfah4m603pm; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat_thief
    ADD CONSTRAINT fkb50y4523m1it0tvfah4m603pm FOREIGN KEY (member_id) REFERENCES public.member(id);


--
-- Name: member_stat_thief fkre2tn8rv1xsie59gre1vo1f0e; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat_thief
    ADD CONSTRAINT fkre2tn8rv1xsie59gre1vo1f0e FOREIGN KEY (grade_thief_id) REFERENCES public.grade_thief(id);


--
-- PostgreSQL database dump complete
--

\unrestrict TfNa5Sy9ixDAuySnGMkouVj2QaHxZvYC3FnK2vUDFJwJBckZfQTyJHtEKP4VCBE

