--
-- PostgreSQL database dump
--

\restrict XAZPLQZMxVXRAXTlzBlldhVIcS0759vgCZMqu3ZplfRSW46r55O3GGbqNHpff2q

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
-- Name: fcm_token; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.fcm_token (
    id bigint NOT NULL,
    member_id bigint NOT NULL,
    value character varying(255) NOT NULL,
    is_active boolean DEFAULT false NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);


ALTER TABLE public.fcm_token OWNER TO root;

--
-- Name: fcm_token_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE public.fcm_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fcm_token_id_seq OWNER TO root;

--
-- Name: fcm_token_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: root
--

ALTER SEQUENCE public.fcm_token_id_seq OWNED BY public.fcm_token.id;


--
-- Name: fcm_token id; Type: DEFAULT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.fcm_token ALTER COLUMN id SET DEFAULT nextval('public.fcm_token_id_seq'::regclass);


--
-- Data for Name: fcm_token; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.fcm_token (id, member_id, value, is_active, is_deleted, created_at, updated_at) FROM stdin;
\.


--
-- Name: fcm_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.fcm_token_id_seq', 1, false);


--
-- Name: fcm_token fcm_token_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.fcm_token
    ADD CONSTRAINT fcm_token_pkey PRIMARY KEY (id);


--
-- Name: fcm_token ukbkc4qj70rm42usijun58k0gi; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.fcm_token
    ADD CONSTRAINT ukbkc4qj70rm42usijun58k0gi UNIQUE (member_id);


--
-- Name: fcm_token fkf1rbjf8lle4r2in6ovkcgl0w8; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.fcm_token
    ADD CONSTRAINT fkf1rbjf8lle4r2in6ovkcgl0w8 FOREIGN KEY (member_id) REFERENCES public.member(id);


--
-- PostgreSQL database dump complete
--

\unrestrict XAZPLQZMxVXRAXTlzBlldhVIcS0759vgCZMqu3ZplfRSW46r55O3GGbqNHpff2q

