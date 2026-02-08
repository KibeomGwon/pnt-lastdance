--
-- PostgreSQL database dump
--

\restrict FzuuUinmR5m9pKiHWxth3CVf167f1ntzBEqQW87k2rSVkgeEe5I8QkyGBcbr0fO

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
-- Name: game_setting; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.game_setting (
    cctv_interval integer,
    is_deleted boolean,
    mission_count integer,
    player_count integer,
    police_count integer,
    prison_lat double precision,
    prison_lng double precision,
    thief_count integer,
    time_limit integer,
    created_at timestamp(6) with time zone NOT NULL,
    game_id bigint NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    boundary_geo public.geometry
);


ALTER TABLE public.game_setting OWNER TO root;

--
-- Data for Name: game_setting; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.game_setting (cctv_interval, is_deleted, mission_count, player_count, police_count, prison_lat, prison_lng, thief_count, time_limit, created_at, game_id, updated_at, boundary_geo) FROM stdin;
1	f	0	4	1	35.1595	129.0604	3	10	2026-01-30 23:07:26.646977+09	2	2026-01-30 23:07:26.646977+09	0103000020E61000000100000004000000D9CEF753E321604014AE47E17A944140448B6CE7FB216040F853E3A59B944140BC749318042260403F355EBA49944140D9CEF753E321604014AE47E17A944140
1	f	0	2	1	35.1595	129.0604	1	2	2026-01-31 20:08:26.534701+09	3	2026-01-31 20:08:26.534701+09	0103000020E61000000100000004000000D9CEF753E321604014AE47E17A944140448B6CE7FB216040F853E3A59B944140BC749318042260403F355EBA49944140D9CEF753E321604014AE47E17A944140
1	f	1	5	1	35.1595	129.0604	4	3	2026-01-30 20:55:53.575836+09	1	2026-01-30 20:55:53.575836+09	0103000020E61000000100000004000000D9CEF753E321604014AE47E17A944140448B6CE7FB216040F853E3A59B944140BC749318042260403F355EBA49944140D9CEF753E321604014AE47E17A944140
0	f	1	5	2	35.1595	129.0604	3	10	2026-02-05 19:59:15.054832+09	4	2026-02-05 19:59:15.054832+09	0103000020E61000000100000004000000D9CEF753E321604014AE47E17A944140448B6CE7FB216040F853E3A59B944140BC749318042260403F355EBA49944140D9CEF753E321604014AE47E17A944140
\.


--
-- Name: game_setting game_setting_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.game_setting
    ADD CONSTRAINT game_setting_pkey PRIMARY KEY (game_id);


--
-- Name: game_setting fk8dfgp8i1cmj89q2b5f8jc14yx; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.game_setting
    ADD CONSTRAINT fk8dfgp8i1cmj89q2b5f8jc14yx FOREIGN KEY (game_id) REFERENCES public.game(id);


--
-- PostgreSQL database dump complete
--

\unrestrict FzuuUinmR5m9pKiHWxth3CVf167f1ntzBEqQW87k2rSVkgeEe5I8QkyGBcbr0fO

