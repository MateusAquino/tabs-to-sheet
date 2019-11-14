package me.mateusaquino.tts;

import java.util.HashMap;
import java.lang.String;

public class MelodyFix {
	final static int offset = 40; // offset para as notas em seus valores MIDI
	
	// Corrige melodia final
	public static String fix(String melody, String instrument){
		return ("V0 I[" + instrument + "] ") 
		     + (melody.trim().replaceAll(" +", " ").replaceAll(" \\+", "+"));
	}
	
	@SuppressWarnings("serial")
	static HashMap<String, Integer> tunes = new HashMap<String, Integer>(){{put("E", 00); put("A", 05); put("D", 10); 
																			put("G", 15); put("B", 19); put("e", 24);}};
	
	// Corrige as notas para seu valor MIDI
	public static String fixNote(String note, String tune, int octaves){
		note = note.replaceAll("h|b|r|p|~|\\/|\\\\", " ").trim(); // remove notações de violão/guitarra
		String notes[] = note.split(" +");
		String fixed = "";
		for (String deathnote : notes){ // Corrige valor para notas tocadas juntas (eg.: 60+64, 24+29+32, ...)
			if (deathnote.contains("+")){
				String junc[] = deathnote.split("\\+");
				String fixedjunc = "";
				for (String jSplit : junc)
					fixedjunc += "+" + newNote(jSplit, tune, octaves);
				fixed += fixedjunc.substring(1) + " ";
			} else // Corrige valor para notas sozinhas (eg.: 60, 24, 42, ...)
				fixed += newNote(deathnote, tune, octaves) + " ";
		}
		return fixed;
	}
	
	// Gera o valor da nova nota baseado na corda tocada, e na oitava escolhida
	private static String newNote(String note, String tune, int octaves){
		int midiID = Integer.parseInt(note) + tunes.get(tune) + offset + 12*octaves;
		String notes[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
		return notes[midiID%12]+(int)Math.floor(midiID/12);
	}
}