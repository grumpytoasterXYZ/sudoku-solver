package de.grumpytoaster.sudoku_solver.model;

import java.util.Arrays;

/**
 * Ein 9×9-Sudoku-Raster.
 *
 * <p>Die 81 Zellen liegen intern in einem eindimensionalen Array. 
 * Zelle ({@code row}, {@code col}) hat den Index {@code row * 9 + col};
 * Zeilen und Spalten zählen von 0 bis 8. Jede Zelle enthält eine Ziffer von 1 bis 9 oder
 * {@link #EMPTY} für ein leeres Feld.
 *
 * <p><b>Textformat</b> für {@link #fromString(String)} und {@link #toCompactString()}: 81 Zeichen, 
 * zeilenweise von links oben nach rechts unten. {@code 1}–{@code 9} sind Ziffern, {@code .}
 * oder {@code 0} leere Zellen. 
 * Beispiel für die erste Zeile: {@code 53..7....}
 *
 * <p>{@code Board} prüft nur Wertebereiche, nicht die Sudoku-Regeln.
 * Doppelte Ziffern in Zeile, Spalte oder Block erkennt der {@code BoardValidator}.
 */
public final class Board {
    
    /** Anzahl der Zeilen und Spalten. */
    public static final int SIZE = 9;

    /** Anzahl der Zellen im Raster. */
    public static final int CELLS = SIZE * SIZE; // 81 = 9 * 9

    /** Wert einer leeren Zelle. */
    public static final int EMPTY = 0;

    /** Zellwerte, Index = {@code row * SIZE + col}. */
    private final int[] cells;

    /** Erzeugt ein leeres Raster, in dem alle Zellen {@link #EMPTY} sind. */
    public Board() {
        this.cells = new int[CELLS];
    }

    /**
     * Übernimmt ein fertig befülltes Array ohne Kopie. 
     * Nur für interne Aufrufe, damit von außen niemand eine Referenz auf {@link #cells} hält.
     */
    private Board(int[] cells) {
        this.cells = cells;
    }

    /**
     * Liest ein Raster aus seinem 81-Zeichen-Textformat.
     *
     * @param s 81 Zeichen; {@code 1}–{@code 9} für Ziffern, {@code .} oder {@code 0} für leere Zellen
     * @return ein neues Board mit diesem Inhalt
     * @throws IllegalArgumentException wenn {@code s} {@code null} ist, nicht
     *         genau 81 Zeichen lang ist oder ein ungültiges Zeichen enthält
     */
    public static Board fromString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String darf nicht null sein");
        }
        if (s.length() != CELLS) {
            throw new IllegalArgumentException("Erwartet sind " + CELLS + " Zeichen, war: " + s.length());
        }
        
        int[] parsed = new int[CELLS];
        
        for (int i = 0; i < CELLS; i++) {
            
            char c = s.charAt(i);
            
            if (c >= '1' && c <= '9') {
                parsed[i] = c - '0';
            }
            else if (c == '0' || c == '.') {
                parsed[i] = EMPTY;
            } 
            else {
                throw new IllegalArgumentException("Ungültiges Zeichen '" + c + "' an Position " + i);
            }
        }
        return new Board(parsed);
    }

    /**
     * Liefert den Wert einer Zelle.
     *
     * @param row Zeile, 0–8
     * @param col Spalte, 0–8
     * @return Ziffer 1–9 oder {@link #EMPTY}
     * @throws IllegalArgumentException wenn {@code row} oder {@code col} außerhalb von 0–8 liegt
     */
    public int get(int row, int col) {
        return cells[index(row, col)];
    }

    /**
     * Setzt den Wert einer Zelle. Mit {@link #EMPTY} wird die Zelle geleert.
     * Geprüft wird nur der Wertebereich, nicht ob die Ziffer die Sudoku-Regeln verletzt.
     *
     * @param row   Zeile, 0–8
     * @param col   Spalte, 0–8
     * @param digit Ziffer 1–9 oder {@link #EMPTY}
     * @throws IllegalArgumentException wenn ein Parameter außerhalb seines Bereichs liegt
     */
    public void set(int row, int col, int digit) {
        if (digit < EMPTY || digit > SIZE) {
            throw new IllegalArgumentException("Werte der Zelle muss zwischen 0 and 9, sie war: " + digit);
        }
        cells[index(row, col)] = digit;
    }

    /**
     * Prüft, ob eine Zelle leer ist.
     *
     * @param row Zeile, 0–8
     * @param col Spalte, 0–8
     * @return {@code true}, wenn die Zelle {@link #EMPTY} enthält
     * @throws IllegalArgumentException wenn {@code row} oder {@code col} außerhalb von 0–8 liegt
     */
    public boolean isEmpty(int row, int col) {
        return get(row, col) == Board.EMPTY;
    }

    /**
     * Erzeugt eine unabhängige Kopie. Änderungen an der Kopie wirken sich nicht
     * auf dieses Board aus und umgekehrt.
     *
     * @return ein neues Board mit demselben Inhalt
     */
    public Board copy() {
        
        int[] newCells = new int[CELLS];
        
        System.arraycopy(cells, 0, newCells, 0, CELLS);
        return new Board(newCells);
    }

    /**
     * Liefert das Raster im 81-Zeichen-Textformat, Gegenstück zu
     * {@link #fromString(String)}. Leere Zellen erscheinen immer als {@code .}.
     *
     * @return 81 Zeichen, zeilenweise
     */
    public String toCompactString() {
        
        StringBuilder sb = new StringBuilder(CELLS);
        
        for (int value : cells) {
            sb.append(symbol(value));
        }
        return sb.toString();
    }

    /**
     * Liefert eine lesbare 9×9-Darstellung mit Trennlinien zwischen den 3×3-Blöcken, gedacht für Debugging und Fehlermeldungen in Tests.
     *
     * @return das Raster über 11 Zeilen
     */
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        for (int row = 0; row < SIZE; row++) {
            if (row > 0 && row % 3 == 0) {
                sb.append("------+-------+------\n");
            }
            
            for (int col = 0; col < SIZE; col++) {
                if (col > 0 && col % 3 == 0) {
                    sb.append("| ");
                }
                sb.append(symbol(get(row, col))).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Zwei Boards sind gleich, wenn alle 81 Zellen übereinstimmen.
     *
     * @param o das zu vergleichende Objekt
     * @return {@code true} bei gleichem Inhalt
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Board other)) {
            return false;
        }
        return Arrays.equals(cells, other.cells);
    }
    
    /**
     * Passend zu {@link #equals(Object)} aus dem Zellinhalt berechnet.
     *
     * @return Hash des Zellinhalts
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(cells);
    }

    /**
     * Rechnet Zeile und Spalte in den Array-Index um und prüft dabei den Bereich.
     * Einzige Stelle mit der Formel {@code row * SIZE + col}.
     */
    private static int index(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException("Zeile und Spalte müssen zwischen 0 und 8 liegen. Sie waren: row: " + row + ", col: " + col);
        }
        return row * SIZE + col;
    }

    /** Zeichen für einen Zellwert: {@code .} für leer, sonst die Ziffer. */
    private static char symbol(int value) {
        return value == EMPTY ? '.' : (char) ('0' + value);
    }
}
