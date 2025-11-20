package academy.log_analyzer.entity;

import java.io.BufferedReader;

public record AnalyzedFile(String fileName, BufferedReader reader) {}
