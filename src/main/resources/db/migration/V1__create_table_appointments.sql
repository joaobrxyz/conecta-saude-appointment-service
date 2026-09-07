CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    data_hora_consulta TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    motivo_cancelamento VARCHAR(255)
);

-- Índices para deixar as buscas muito mais rápidas na hora de validar choque de horários
CREATE INDEX idx_appointment_doctor ON appointments(doctor_id);
CREATE INDEX idx_appointment_patient ON appointments(patient_id);
CREATE INDEX idx_appointment_data ON appointments(data_hora_consulta);