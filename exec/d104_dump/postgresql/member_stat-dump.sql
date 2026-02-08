--
-- PostgreSQL database dump
--

\restrict XVx5wmJ2xTVTwZX3eW9lCjSJHmLbssP62lRpkdnmJRZzP6JY61xX0SUj2xFHnFb

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
-- Name: member_stat; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.member_stat (
    is_deleted boolean,
    loses integer,
    police_game integer,
    thief_game integer,
    total_games integer,
    wins integer,
    created_at timestamp(6) with time zone NOT NULL,
    member_id bigint NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.member_stat OWNER TO root;

--
-- Data for Name: member_stat; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.member_stat (is_deleted, loses, police_game, thief_game, total_games, wins, created_at, member_id, updated_at) FROM stdin;
f	7	3	3	10	3	2026-01-31 20:12:47.667495+09	6	2026-01-31 20:34:31.430195+09
f	11	0	32	32	21	2026-01-31 17:25:20.294217+09	4	2026-02-02 20:58:53.296699+09
f	21	14	3	25	4	2026-01-31 17:25:20.296334+09	5	2026-02-02 20:58:53.296792+09
f	17	12	21	33	16	2026-01-31 17:25:20.276284+09	1	2026-02-05 20:52:32.903294+09
f	11	0	33	33	22	2026-01-31 17:25:20.288152+09	2	2026-02-05 20:52:32.931955+09
f	11	0	33	33	22	2026-01-31 17:25:20.291916+09	3	2026-02-05 20:52:33.16741+09
\.


--
-- Name: member_stat member_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat
    ADD CONSTRAINT member_stat_pkey PRIMARY KEY (member_id);


--
-- Name: member_stat fk3fmk16ca657v360fpwpb2x5w1; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_stat
    ADD CONSTRAINT fk3fmk16ca657v360fpwpb2x5w1 FOREIGN KEY (member_id) REFERENCES public.member(id);


--
-- PostgreSQL database dump complete
--

\unrestrict XVx5wmJ2xTVTwZX3eW9lCjSJHmLbssP62lRpkdnmJRZzP6JY61xX0SUj2xFHnFb

