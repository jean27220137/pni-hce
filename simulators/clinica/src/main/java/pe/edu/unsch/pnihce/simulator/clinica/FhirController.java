package pe.edu.unsch.pnihce.simulator.clinica;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/fhir")
public class FhirController {

    private static final String SYSTEM = "http://clinica-clini0001.privado.pe/his/patients";

    private static final Map<String, PatientRecord> PATIENTS = Map.of(
        "CLINI-HC-000101", new PatientRecord(
            "CLINI-HC-000101", "Flores Torres", "María", "female", "1985-03-15",
            new ConditionRecord[]{ new ConditionRecord("C1", "M54.5", "Lumbalgia", "2026-09-15"),
                                   new ConditionRecord("C2", "F41.1", "Trastorno de ansiedad generalizada", "2025-06-01") }),
        "CLINI-HC-000102", new PatientRecord(
            "CLINI-HC-000102", "Medina Quispe", "Rosa", "female", "1990-11-03",
            new ConditionRecord[]{ new ConditionRecord("C1", "O80", "Parto único espontáneo", "2026-08-20") })
    );

    @GetMapping(value = "/Patient/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPatient(@PathVariable String id) {
        PatientRecord p = PATIENTS.get(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildPatient(p));
    }

    @GetMapping(value = "/Patient/{id}/$summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSummary(@PathVariable String id) {
        PatientRecord p = PATIENTS.get(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildBundle(p));
    }

    private Map<String, Object> buildPatient(PatientRecord p) {
        return Map.of(
            "resourceType", "Patient",
            "id", p.id(),
            "identifier", new Object[]{ Map.of("system", SYSTEM, "value", p.id()) },
            "name", new Object[]{ Map.of("use", "official", "family", p.family(), "given", new String[]{ p.given() }) },
            "gender", p.gender(),
            "birthDate", p.birthDate()
        );
    }

    private Map<String, Object> buildBundle(PatientRecord p) {
        var entries = new ArrayList<Map<String, Object>>();
        entries.add(Map.of("resource", buildPatient(p)));
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
