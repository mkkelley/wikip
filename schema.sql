CREATE DATABASE wikip;
CREATE USER wikiuser WITH PASSWORD 'testing';
GRANT ALL ON DATABASE wikip TO wikiuser;
\c wikip
--
-- Name: links; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE links (
    id integer NOT NULL,
    frompage integer NOT NULL,
    topage integer NOT NULL
);


ALTER TABLE public.links OWNER TO postgres;

--
-- Name: links_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE links_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.links_id_seq OWNER TO postgres;

--
-- Name: links_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE links_id_seq OWNED BY links.id;


--
-- Name: pages; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE pages (
    id integer NOT NULL,
    title text NOT NULL
);


ALTER TABLE public.pages OWNER TO postgres;

--
-- Name: results; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE results (
    id integer NOT NULL,
    frompage integer NOT NULL,
    topage integer NOT NULL,
    indirection integer NOT NULL,
    max integer NOT NULL,
    timetaken integer NOT NULL
);


ALTER TABLE public.results OWNER TO postgres;

--
-- Name: results_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE results_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.results_id_seq OWNER TO postgres;

--
-- Name: results_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE results_id_seq OWNED BY results.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY links ALTER COLUMN id SET DEFAULT nextval('links_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY results ALTER COLUMN id SET DEFAULT nextval('results_id_seq'::regclass);

--
-- Name: links_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY links
    ADD CONSTRAINT links_pkey PRIMARY KEY (id);


--
-- Name: pages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY pages
    ADD CONSTRAINT pages_pkey PRIMARY KEY (id);


--
-- Name: results_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY results
    ADD CONSTRAINT results_pkey PRIMARY KEY (id);


--
-- Name: from_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX from_index ON links USING btree (frompage);


--
-- Name: title_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX title_index ON pages USING btree (title);


--
-- Name: to_from_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX to_from_index ON links USING btree (frompage, topage);


--
-- Name: to_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX to_index ON links USING btree (topage);


--
-- Name: links_frompage_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY links
    ADD CONSTRAINT links_frompage_fkey FOREIGN KEY (frompage) REFERENCES pages(id) ON DELETE CASCADE;


--
-- Name: links_topage_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY links
    ADD CONSTRAINT links_topage_fkey FOREIGN KEY (topage) REFERENCES pages(id) ON DELETE CASCADE;


--
-- Name: results_frompage_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY results
    ADD CONSTRAINT results_frompage_fkey FOREIGN KEY (frompage) REFERENCES pages(id);


--
-- Name: results_topage_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY results
    ADD CONSTRAINT results_topage_fkey FOREIGN KEY (topage) REFERENCES pages(id);

GRANT ALL ON results TO wikiuser;
GRANT ALL ON results_id_seq TO wikiuser;
GRANT ALL ON links TO wikiuser;
GRANT ALL ON links_id_seq TO wikiuser;
GRANT ALL ON pages TO wikiuser;

