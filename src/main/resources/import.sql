-- Initialization script for MES Master Data
-- This script is automatically executed by Hibernate/JPA when
-- spring.jpa.hibernate.ddl-auto is set to 'create' or 'create-drop'.

-- Insert Tools (T1 to T6)
INSERT INTO tools (tool_name) VALUES ('T1');
INSERT INTO tools (tool_name) VALUES ('T2');
INSERT INTO tools (tool_name) VALUES ('T3');
INSERT INTO tools (tool_name) VALUES ('T4');
INSERT INTO tools (tool_name) VALUES ('T5');
INSERT INTO tools (tool_name) VALUES ('T6');

-- Insert Unloading Docks (U1 to U4)
INSERT INTO unloading_docks (dock_name) VALUES ('U1');
INSERT INTO unloading_docks (dock_name) VALUES ('U2');
INSERT INTO unloading_docks (dock_name) VALUES ('U3');
INSERT INTO unloading_docks (dock_name) VALUES ('U4');

-- Insert Machines (M1a to M6b)
INSERT INTO machines (machine_name) VALUES ('M1a');
INSERT INTO machines (machine_name) VALUES ('M1b');
INSERT INTO machines (machine_name) VALUES ('M2a');
INSERT INTO machines (machine_name) VALUES ('M2b');
INSERT INTO machines (machine_name) VALUES ('M3a');
INSERT INTO machines (machine_name) VALUES ('M3b');
INSERT INTO machines (machine_name) VALUES ('M4a');
INSERT INTO machines (machine_name) VALUES ('M4b');
INSERT INTO machines (machine_name) VALUES ('M5a');
INSERT INTO machines (machine_name) VALUES ('M5b');
INSERT INTO machines (machine_name) VALUES ('M6a');
INSERT INTO machines (machine_name) VALUES ('M6b');

-- UPDATE machines SET current_tool_id = (SELECT id FROM tools WHERE tool_name = 'T1') WHERE machine_name = 'M1a';