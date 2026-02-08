--
-- PostgreSQL database dump
--

\restrict exvDcxBdvXJyB1wcgf9ZwrWL4B0yE2gbrFyaJJTMtTQ5lFQu8j26rcE64B7n4yL

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
-- Name: member_profile; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.member_profile (
    is_deleted boolean,
    created_at timestamp(6) with time zone NOT NULL,
    member_id bigint NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    nickname character varying(20),
    avatar_url character varying(2048)
);


ALTER TABLE public.member_profile OWNER TO root;

--
-- Data for Name: member_profile; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.member_profile (is_deleted, created_at, member_id, updated_at, nickname, avatar_url) FROM stdin;
f	2026-01-30 20:45:08.997898+09	1	2026-01-30 20:45:08.997898+09	gkgk	default.png
f	2026-01-30 20:45:15.589337+09	2	2026-01-30 20:45:15.589337+09	bbbbb	default.png
f	2026-01-30 20:45:22.508254+09	3	2026-01-30 20:45:22.508254+09	ccccc	default.png
f	2026-01-30 20:45:29.935065+09	4	2026-01-30 20:45:29.935065+09	ddddd	default.png
f	2026-01-30 20:45:36.062914+09	5	2026-01-30 20:45:36.062914+09	eeeee	default.png
f	2026-01-30 23:06:28.072474+09	6	2026-01-30 23:06:28.072474+09	asdfasdf	default.png
f	2026-01-31 20:07:45.588658+09	7	2026-01-31 20:07:45.588658+09	asdfasdf	default.png
f	2026-01-31 20:08:43.904274+09	8	2026-01-31 20:08:43.904274+09	asdfasdf	default.png
\.


--
-- Name: member_profile member_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_profile
    ADD CONSTRAINT member_profile_pkey PRIMARY KEY (member_id);


--
-- Name: member_profile fkkrtcs7wdtv954e99w88uga9mq; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.member_profile
    ADD CONSTRAINT fkkrtcs7wdtv954e99w88uga9mq FOREIGN KEY (member_id) REFERENCES public.member(id);


--
-- PostgreSQL database dump complete
--

\unrestrict exvDcxBdvXJyB1wcgf9ZwrWL4B0yE2gbrFyaJJTMtTQ5lFQu8j26rcE64B7n4yL

