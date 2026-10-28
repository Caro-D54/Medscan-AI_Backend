package com.medscan.app_med.service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor inteligente de datos farmacéuticos (nombre comercial, principio activo, dosis, frecuencia)
 * a partir de texto no estructurado reconocido por OCR.
 */
public final class MedicationTextExtractor {

    private record VademecumItem(String brandName, String activeIngredient, List<String> aliases) {}

    private static final List<VademecumItem> VADEMECUM = List.of(
            new VademecumItem("Tafirol", "Paracetamol", List.of("tafirol", "tafirol forte", "tafirol 1g", "tafirol 500")),
            new VademecumItem("Termofren", "Paracetamol", List.of("termofren", "panadol", "dirox")),
            new VademecumItem("Paracetamol", "Paracetamol", List.of("paracetamol", "acetaminofen", "acetaminophen")),
            new VademecumItem("Ibupirac", "Ibuprofeno", List.of("ibupirac", "ibupirac 400", "ibupirac 600", "ibupirac fem")),
            new VademecumItem("Actron", "Ibuprofeno", List.of("actron", "actron 400", "actron 600", "actron rapido")),
            new VademecumItem("Fabogesic", "Ibuprofeno", List.of("fabogesic", "ibucler", "ibumar")),
            new VademecumItem("Ibuprofeno", "Ibuprofeno", List.of("ibuprofeno", "ibuprofen")),
            new VademecumItem("Amoxidal", "Amoxicilina", List.of("amoxidal", "amoxidal duo", "amoxidal 500", "amoxidal 875")),
            new VademecumItem("Optamox", "Amoxicilina / Ácido Clavulánico", List.of("optamox", "optamox duo", "augmentin", "amixen")),
            new VademecumItem("Amoxicilina", "Amoxicilina", List.of("amoxicilina", "amoxicillin")),
            new VademecumItem("Bayaspirina", "Ácido Acetilsalicílico", List.of("bayaspirina", "aspirineta", "aspirina", "ecotrin")),
            new VademecumItem("Novalgina", "Dipirona", List.of("novalgina", "dipirona")),
            new VademecumItem("Buscapina", "Hioscina N-butilbromuro", List.of("buscapina", "buscapina compositum", "hioscina")),
            new VademecumItem("Sertal", "Propinox / Clonixinato de lisina", List.of("sertal", "sertal compuesto", "sertal perlas")),
            new VademecumItem("Dorixina", "Clonixinato de lisina", List.of("dorixina", "clonixinato")),
            new VademecumItem("Oxa", "Diclofenac sódico", List.of("oxa", "oxa b12", "dioxaflex", "voltaren", "diclofenac")),
            new VademecumItem("Dolten", "Ketorolac", List.of("dolten", "sinalgico", "ketorolac", "keterolac")),
            new VademecumItem("Acimed", "Omeprazol", List.of("acimed", "ulpax", "losec", "faboprazol", "omeprazol")),
            new VademecumItem("Pantoprazol", "Pantoprazol", List.of("pantoprazol", "pantus", "peptazol", "esomeprazol", "nexium")),
            new VademecumItem("Lotrial", "Enalapril", List.of("lotrial", "gadopril", "renitec", "enalapril")),
            new VademecumItem("Losacor", "Losartán potásico", List.of("losacor", "cozaar", "loplok", "losartan", "losartán")),
            new VademecumItem("Amloc", "Amlodipina", List.of("amloc", "pelba", "norvasc", "amlodipina")),
            new VademecumItem("Rivotril", "Clonazepam", List.of("rivotril", "clonagin", "diocam", "clonazepam")),
            new VademecumItem("Alplax", "Alprazolam", List.of("alplax", "xanax", "tranquinal", "alprazolam")),
            new VademecumItem("Zoloft", "Sertralina", List.of("zoloft", "atenix", "sertralina")),
            new VademecumItem("Lexapro", "Escitalopram", List.of("lexapro", "meridian", "escitalopram")),
            new VademecumItem("Aerotina", "Loratadina", List.of("aerotina", "clarityne", "loratadina")),
            new VademecumItem("Allegra", "Fexofenadina", List.of("allegra", "fexofenadina")),
            new VademecumItem("Qura Plus", "Paracetamol / Pseudoefedrina / Clorfeniramina", List.of("qura plus", "qura", "curaplus", "refrianex")),
            new VademecumItem("Migral", "Ergotamina / Dipirona / Cafeína", List.of("migral", "tetralgin")),
            new VademecumItem("Glucophage", "Metformina", List.of("glucophage", "dbi", "metformina")),
            new VademecumItem("T4 Montpellier", "Levotiroxina sódica", List.of("t4 montpellier", "t4", "euthyrox", "levotiroxina"))
    );

    private static final Pattern DOSAGE_PATTERN = Pattern.compile(
            "\\b(\\d+(?:[.,]\\d+)?\\s*(?:mg|g|mcg|ml|ui|%))\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern FREQUENCY_PATTERN = Pattern.compile(
            "\\b(cada\\s+\\d+\\s+horas?|cada\\s+\\d+\\s+hs|\\d+\\s*(?:veces|comprimidos?|tomas?)\\s*al\\s*d[ií]a|cada\\s+d[ií]a|1\\s*por\\s*d[ií]a|cada\\s+mañana|cada\\s+noche)\\b", Pattern.CASE_INSENSITIVE);

    private static final List<String> NOISE_WORDS = List.of(
            "venta", "bajo", "receta", "libre", "industria", "argentina", "uso", "oral",
            "comprimidos", "recubiertos", "capsulas", "blandas", "tabletas", "jarabe",
            "gotas", "contenido", "contiene", "envase", "laboratorio", "laboratorios",
            "bago", "roemmers", "elea", "raffo", "gador", "bayer", "pfizer"
    );

    private MedicationTextExtractor() {}

    public static ScannedMedication extract(String rawOcrText) {
        if (rawOcrText == null || rawOcrText.isBlank()) {
            return new ScannedMedication("", "", "", "");
        }

        String normalized = normalize(rawOcrText);

        String matchedBrand = null;
        String matchedIngredient = null;

        // 1. Buscar en vademécum
        for (VademecumItem item : VADEMECUM) {
            for (String alias : item.aliases()) {
                String normAlias = normalize(alias);
                Pattern p = Pattern.compile("\\b" + Pattern.quote(normAlias) + "\\b");
                if (p.matcher(normalized).find()) {
                    matchedIngredient = item.activeIngredient();
                    if (!normAlias.equals(normalize(item.activeIngredient()))) {
                        matchedBrand = item.brandName();
                        break;
                    } else if (matchedBrand == null) {
                        matchedBrand = item.brandName();
                    }
                }
            }
            if (matchedBrand != null && !matchedBrand.equalsIgnoreCase(matchedIngredient)) {
                break;
            }
        }

        // 2. Extraer dosis
        String matchedDosage = "";
        Matcher dosageMatcher = DOSAGE_PATTERN.matcher(rawOcrText);
        if (dosageMatcher.find()) {
            matchedDosage = dosageMatcher.group(1).trim();
        }

        // 3. Extraer frecuencia
        String matchedFrequency = "";
        Matcher freqMatcher = FREQUENCY_PATTERN.matcher(rawOcrText);
        if (freqMatcher.find()) {
            matchedFrequency = freqMatcher.group(1).trim();
        }

        // 4. Si no se encontró marca en vademécum, buscar líneas candidatas en el texto OCR
        if (matchedBrand == null || matchedBrand.isBlank()) {
            String[] lines = rawOcrText.split("\\r?\\n");
            for (String line : lines) {
                String clean = cleanLine(line);
                if (clean.length() >= 3 && clean.length() <= 30) {
                    matchedBrand = toTitleCase(clean);
                    if (matchedIngredient == null || matchedIngredient.isBlank()) {
                        matchedIngredient = matchedBrand;
                    }
                    break;
                }
            }
        }

        if (matchedBrand == null) {
            matchedBrand = "";
        }
        if (matchedIngredient == null) {
            matchedIngredient = "";
        }

        return new ScannedMedication(matchedBrand, matchedIngredient, matchedDosage, matchedFrequency);
    }

    private static String normalize(String input) {
        if (input == null) return "";
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        n = n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return n.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private static String cleanLine(String line) {
        String n = line.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]", " ").replaceAll("\\s+", " ").trim();
        for (String noise : NOISE_WORDS) {
            n = n.replaceAll("(?i)\\b" + noise + "\\b", "").trim();
        }
        return n.replaceAll("\\s+", " ").trim();
    }

    private static String toTitleCase(String text) {
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}
