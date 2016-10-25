-- Table

CREATE TABLE IF NOT EXISTS color_collections (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  modified_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  deleted_at timestamptz DEFAULT NULL,

  deleted boolean DEFAULT false,
  version bigint NOT NULL DEFAULT 0,

  "user" uuid REFERENCES users(id),
  name text NOT NULL,
  data bytea NOT NULL
) WITH (OIDS=FALSE);

-- Triggers

CREATE TRIGGER color_collections_occ_tgr BEFORE UPDATE ON color_collections
  FOR EACH ROW EXECUTE PROCEDURE handle_occ();

CREATE TRIGGER color_collections_modified_at_tgr BEFORE UPDATE ON color_collections
  FOR EACH ROW EXECUTE PROCEDURE update_modified_at();

-- Indexes

CREATE INDEX deleted_color_collections_idx
  ON color_collections USING btree (deleted)
  WHERE deleted = true;
