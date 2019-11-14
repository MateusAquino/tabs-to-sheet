package me.mateusaquino.tts;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Guitar {
	private java.lang.String melody = "";
	
	public Guitar(java.lang.String tab, int octaves){
		java.lang.String[] strings = tab.trim().split("\n");
		for (int tabOffset = 0; tabOffset < strings.length-5; tabOffset+=6){
			String[] guitarStrings = {new String("e"),new String("B"),new String("G"),new String("D"),new String("A"),new String("E")};
			while (!strings[tabOffset].trim().matches("^\\w\\|[^|]+?\\|"))
				tabOffset++;
			HashMap<java.lang.String, char[]> map = new HashMap<java.lang.String, char[]>();
			int length = getLine(strings[tabOffset+0]).length;
			map.put("e", getLine(strings[tabOffset+0]));
			map.put("B", getLine(strings[tabOffset+1]));
			map.put("G", getLine(strings[tabOffset+2]));
			map.put("D", getLine(strings[tabOffset+3]));
			map.put("A", getLine(strings[tabOffset+4]));
			map.put("E", getLine(strings[tabOffset+5]));
					
			int oldpos = -1;
			java.lang.String oldnote = "";
			while (!isDone(length, guitarStrings)){ // enquanto nao terminar a tab
				String string = guitarStrings[0];
				char[] curLine = map.get(string.stringName); // pega a corda atual da tab
				java.lang.String toAdd = "";
				while(curLine[++string.pos]!='-' && curLine[string.pos]!='|') // le o valor da nota (13, 2h4p2, 8/9,...)
					toAdd += curLine[string.pos]; // toAdd possui a nota lida na corda tab
				
				if (!toAdd.isEmpty()){
					toAdd = MelodyFix.fixNote(toAdd, string.stringName, octaves); // corrige a nota para seu valor MIDI
					
					if (oldpos==string.pos)    // se duas notas tiverem na msma posição -> tocam juntas
						oldnote += "+"+toAdd;
					else {
						if (!oldnote.isEmpty())
							melody += oldnote + " "; // adiciona a nota na melodia final
						oldnote = toAdd;
					}
					oldpos = string.pos;
				}
				guitarStrings = sort(guitarStrings); // ordena as cordas p/ poder ler em ordem
			}
			melody += oldnote + " R ";
		}
	}
	
	Pattern lineRgx = Pattern.compile("^\\w\\|[^|]+?\\|");
	public char[] getLine(java.lang.String str){
		Matcher m = lineRgx.matcher(str.trim()); 
		m.find();
		return m.group(0).toCharArray();
	}
	
	public java.lang.String toMelody(){
		return melody;
	}
	
	private boolean isDone(int length, String[] guitarStrings){
		for (String string : guitarStrings)
			if (string.pos<length-1)
				return false;
		return true;
	}
	
	// Bubble sort para ordenar as cordas da tab
	private String[] sort(String[] strings){
		boolean houveTroca = true;
		while(houveTroca){
			houveTroca = false;
			for (int i = 0; i<6-1; i++)
				if (strings[i].pos>strings[i+1].pos){
					String temp = strings[i];
					strings[i] = strings[i+1];
					strings[i+1] = temp;
					houveTroca = true;
				}
		}
		return strings;
	}
}