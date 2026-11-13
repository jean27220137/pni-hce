package pe.edu.unsch.pnihce.simulator.posta;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fhir")
public class FhirController {

    private static final String SYSTEM = "http://posta-posta0001.minsa.gob.pe/his/patients";

    // Pacientes simulados: idLocal → datos FHIR
    private static final Map<String, PatientRecord> PATIENTS = Map.of(
        "POSTA-HC-000981", new PatientRecord(
            "POSTA-HC-000981", "Flores Torres", "María", "female", "1985-03-15",
            new ConditionRecord[]{ new ConditionRecord("C1", "J18.9", "Neumonía no especificada", "2026-10-05"),
                                   new ConditionRecord("C2", "I10",   "Hipertensión esencial",   "2024-03-15") }),
        "POSTA-HC-000982", new PatientRecord(
            "POSTA-HC-000982", "Ayala Quispe", "Juan Carlos", "male", "1972-07-22",
            new ConditionRecord[]{ new ConditionRecord("C1", "E11",   "Diabetes mellitus tipo 2", "2023-01-20") })
    );

    @GetMapping(value = "/Patient/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPatient(@PathVariable String id) {
        PatientRecord p = PATIENTS.get(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildPatient(p, SYSTEM));
    }

    @GetMapping(value = "/Patient/{id}/$summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSummary(@PathVariable String id) {
        PatientRecord p = PATIENTS.get(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildBundle(p, SYSTEM));
    }

    static Map<String, Object> buildPatient(PatientRecord p, String system) {
        return Map.of(
            "resourceType", "Patient",
            "id", p.id(),
            "identifier", new Object[]{ Map.of("system", system, "value", p.id()) },
            "name", new Object[]{ Map.of("use", "official", "family", p.family(), "given", new String[]{ p.given() }) },
            "gender", p.gender(),
            "birthDate", p.birthDate()
        );
    }

    static Map<String, Object> buildBundle(PatientRecord p, String system) {
        var entries = new java.util.ArrayList<Map<String, Object>>();
        entries.add(Map.of("resource", buildPatient(p, system)));
        for (var c : p.conditions()) {
            entries.add(Map.of("resource", Map.of(
                "resourceType", "Condition",
                "id", c.id(),
                "subject", Map.of("reference", "Patient/" + p.id()),
                "code", Map.of("coding", new Object[]{ Map.of("system", "http://hl7.org/fhir/sid/icd-10", "code", c.code(), "display", c.display()) }),
                "onsetDateTime", c.onset()
            )));
        }
        return Map.of(
            "resourceType", "Bundle",
            "id", "summary-" + p.id(),
            "type", "searchset",
            "total", entries.size(),
            "entry", entries
        );
    }

    record PatientRecord(String id, String family, String given, String gender, String birthDate, ConditionRecord[] conditions) {}
    record ConditionRecord(String id, String code, String display, String onset) {}
}
