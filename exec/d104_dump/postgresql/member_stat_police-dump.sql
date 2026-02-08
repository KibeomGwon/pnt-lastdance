--
-- PostgreSQL database dump
--

\restrict moWiPBg9oM8FLQPzLyEFOrhj9o4HmVdwdkTNOhJN2f03SpG9vNo9kcbUQHg7wim

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
-- Name: member_stat_police; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.member_stat_police (
    is_deleted boolean,
    most_arrests_in_game integer,
    total_arrest_count integer,
    created_at timestamp(6) with time zone NOT NULL,
    grade_police_id bigint,
    member_id bigint NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.member_stat_police OWNER TO root;

--
-- Data for Name: member_stat_police; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.member_stat_police (is_deleted, most_arrests_in_game, total_arrest_count, created_at, grade_police_id, member_id, updated_at) FROM stdin;
f	0	0	2026-01-30 20:45:15.592685+09	1	2	2026-01-30 20:45:15.592685+09
f	0	0	2026-01-30 20:45:22.513502+09	1	3	2026-01-30 20:45:22.513502+09
f	0	0	2026-01-30 20:45:29.947163+09	1	4	2026-01-30 20:45:29.947163+09
f	0	0	2026-01-31 20:07:45.640119+09	1	7	2026-01-31 20:07:45.640119+09
f	0	0	2026-01-31 20:08:43.908465+09	1	8	2026-01-31 20:08:43.908465+09
f	0	0	2026-01-30 23:06:28.109757+09	2	6	2026-01-31 20:34:31.430212+09
f	0	0	2026-01-30 20:45:09.033264+09	2	1	2026-01-31 20:49:55.006528+09
f	5	18	2026-01-30 20:45:36.065742+09	1	5	2026-02-02 01:16:15.712567+09
\.


--
-- Name: member_stat_police member_stat_police_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat_police
    ADD CONSTRAINT member_stat_police_pkey PRIMARY KEY (member_id);


--
-- Name: member_stat_police fk1xmwmvilsve3995jepw0o3nnl; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat_police
    ADD CONSTRAINT fk1xmwmvilsve3995jepw0o3nnl FOREIGN KEY (grade_police_id) REFERENCES public.grade_police(id);


--
-- Name: member_stat_police fkdsbs4bb1taoaryuq79v7l4dbn; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat_police
    ADD CONSTRAINT fkdsbs4bb1taoaryuq79v7l4dbn FOREIGN KEY (member_id) REFERENCES public.member(id);


--
-- PostgreSQL database dump complete
--

\unrestrict moWiPBg9oM8FLQPzLyEFOrhj9o4HmVdwdkTNOhJN2f03SpG9vNo9kcbUQHg7wim

