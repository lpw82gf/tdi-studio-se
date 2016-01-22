package com.sforce.soap.partner;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated by SimpleTypeCodeGenerator.java. Please do not edit.
 */
public enum CaseType {


  
	/**
	 * Enumeration  : Nominative
	 */
	Nominative("Nominative"),

  
	/**
	 * Enumeration  : Accusative
	 */
	Accusative("Accusative"),

  
	/**
	 * Enumeration  : Genitive
	 */
	Genitive("Genitive"),

  
	/**
	 * Enumeration  : Dative
	 */
	Dative("Dative"),

  
	/**
	 * Enumeration  : Inessive
	 */
	Inessive("Inessive"),

  
	/**
	 * Enumeration  : Elative
	 */
	Elative("Elative"),

  
	/**
	 * Enumeration  : Illative
	 */
	Illative("Illative"),

  
	/**
	 * Enumeration  : Adessive
	 */
	Adessive("Adessive"),

  
	/**
	 * Enumeration  : Ablative
	 */
	Ablative("Ablative"),

  
	/**
	 * Enumeration  : Allative
	 */
	Allative("Allative"),

  
	/**
	 * Enumeration  : Essive
	 */
	Essive("Essive"),

  
	/**
	 * Enumeration  : Translative
	 */
	Translative("Translative"),

  
	/**
	 * Enumeration  : Partitive
	 */
	Partitive("Partitive"),

  
	/**
	 * Enumeration  : Objective
	 */
	Objective("Objective"),

  
	/**
	 * Enumeration  : Subjective
	 */
	Subjective("Subjective"),

  
	/**
	 * Enumeration  : Instrumental
	 */
	Instrumental("Instrumental"),

  
	/**
	 * Enumeration  : Prepositional
	 */
	Prepositional("Prepositional"),

  
	/**
	 * Enumeration  : Locative
	 */
	Locative("Locative"),

  
	/**
	 * Enumeration  : Vocative
	 */
	Vocative("Vocative"),

  
	/**
	 * Enumeration  : Sublative
	 */
	Sublative("Sublative"),

  
	/**
	 * Enumeration  : Superessive
	 */
	Superessive("Superessive"),

  
	/**
	 * Enumeration  : Delative
	 */
	Delative("Delative"),

  
	/**
	 * Enumeration  : Causalfinal
	 */
	Causalfinal("Causalfinal"),

  
	/**
	 * Enumeration  : Essiveformal
	 */
	Essiveformal("Essiveformal"),

  
	/**
	 * Enumeration  : Termanative
	 */
	Termanative("Termanative"),

  
	/**
	 * Enumeration  : Distributive
	 */
	Distributive("Distributive"),

  
	/**
	 * Enumeration  : Ergative
	 */
	Ergative("Ergative"),

  
	/**
	 * Enumeration  : Adverbial
	 */
	Adverbial("Adverbial"),

  
	/**
	 * Enumeration  : Abessive
	 */
	Abessive("Abessive"),

  
	/**
	 * Enumeration  : Comitative
	 */
	Comitative("Comitative"),

;

	public static Map<String, String> valuesToEnums;

	static {
   		valuesToEnums = new HashMap<String, String>();
   		for (CaseType e : EnumSet.allOf(CaseType.class)) {
   			valuesToEnums.put(e.toString(), e.name());
   		}
   	}

   	private String value;

   	private CaseType(String value) {
   		this.value = value;
   	}

   	@Override
   	public String toString() {
   		return value;
   	}
}