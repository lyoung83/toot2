/* Generated by Together */

package uk.org.toot.pitch;

public class Interval {
    public final static int UNISON = 0 ;
    public final static int MINOR_SECOND = 1 ;
    public final static int MAJOR_SECOND = 2 ;
    public final static int MINOR_THIRD = 3 ;
    public final static int MAJOR_THIRD = 4 ;
    public final static int PERFECT_FOURTH = 5 ;
    public final static int AUGMENTED_FOURTH = 6 ;
    public final static int DIMINISHED_FIFTH = AUGMENTED_FOURTH ;
    public final static int PERFECT_FIFTH = 7 ;
    public final static int AUGMENTED_FIFTH = 8 ;
    public final static int MINOR_SIXTH = AUGMENTED_FIFTH ;
    public final static int MAJOR_SIXTH = 9 ;
    public final static int DIMINISHED_SEVENTH = MAJOR_SIXTH ;
    public final static int MINOR_SEVENTH = 10 ;
    public final static int MAJOR_SEVENTH = 11 ;
    public final static int OCTAVE = 12 ;

    private Interval() {
        // prevent instantiation
    }

    public static String name(int interval) {
        switch ( interval ) {
        case UNISON: return "Unison";
        case MINOR_SECOND: return "Minor second";
        case MAJOR_SECOND: return "Major second";
        case MINOR_THIRD: return "Minor third";
        case MAJOR_THIRD: return "Major third";
        case PERFECT_FOURTH: return "Perfect fourth";
        case DIMINISHED_FIFTH: return "Diminished fifth";
        case PERFECT_FIFTH: return "Perfect fifth";
        case AUGMENTED_FIFTH: return "Augmented fifth";
//        case MINOR_SIXTH: return "Minor sixth";
        case MAJOR_SIXTH: return "Major sixth";
        case MINOR_SEVENTH: return "Minor seventh";
        case MAJOR_SEVENTH: return "Major seventh";
        case OCTAVE: return "Octave";
        }
        return "{"+String.valueOf(interval)+"}";
    }

    public static String numeral(int interval) {
        switch ( interval ) {
        case UNISON: return "I";
        case MINOR_SECOND: return "ii";
        case MAJOR_SECOND: return "II";
        case MINOR_THIRD: return "iii";
        case MAJOR_THIRD: return "III";
        case PERFECT_FOURTH: return "IV";
        case DIMINISHED_FIFTH: return "vo";
        case PERFECT_FIFTH: return "V";
        case AUGMENTED_FIFTH: return "V+";
//        case MINOR_SIXTH: return "vi";
        case MAJOR_SIXTH: return "VI";
        case MINOR_SEVENTH: return "vii";
        case MAJOR_SEVENTH: return "VII";
        case OCTAVE: return "I"; // !!! ??
        }
        return "??";
    }

    public static int spelt(String s) {
		if ( s.equals("1") ) return UNISON;
        else if ( s.equals("b2") ) return MINOR_SECOND;
        else if ( s.equals("2") ) return MAJOR_SECOND;
        else if ( s.equals("b3") ) return MINOR_THIRD;
        else if ( s.equals("3") ) return MAJOR_THIRD;
        else if ( s.equals("4") ) return PERFECT_FOURTH;
        else if ( s.equals("#4") ) return AUGMENTED_FOURTH;
        else if ( s.equals("b5") ) return DIMINISHED_FIFTH;
        else if ( s.equals("5") ) return PERFECT_FIFTH;
        else if ( s.equals("#5") ) return AUGMENTED_FIFTH;
        else if ( s.equals("b6") ) return MINOR_SIXTH;
        else if ( s.equals("6") ) return MAJOR_SIXTH;
        else if ( s.equals("bb7") ) return DIMINISHED_SEVENTH;
        else if ( s.equals("b7") ) return MINOR_SEVENTH;
        else if ( s.equals("7") ) return MAJOR_SEVENTH;
        else if ( s.equals("b9") ) return OCTAVE+MINOR_SECOND;
        else if ( s.equals("9") ) return OCTAVE+MAJOR_SECOND;
        else if ( s.equals("#9") ) return OCTAVE+MINOR_THIRD;
        else if ( s.equals("b11") ) return OCTAVE+MAJOR_THIRD;
        else if ( s.equals("11") ) return OCTAVE+PERFECT_FOURTH;
        else if ( s.equals("#11") ) return OCTAVE+DIMINISHED_FIFTH;
        else if ( s.equals("b13") ) return OCTAVE+MINOR_SIXTH; // !!!
        else if ( s.equals("13") ) return OCTAVE+MAJOR_SIXTH;
        else return -1; // !!!
    }

    public static String spell(int[] intervals) {
        StringBuffer spelling = new StringBuffer();
        for ( int i = 0; i < intervals.length; i++ ) {
            spelling.append(spell(intervals[i]));
            spelling.append(" ");
        }
        return spelling.toString();
    }

    public static String spell(int interval) {
        switch ( interval ) {
        case UNISON: return "I";
        case MINOR_SECOND: return "b2";
        case MAJOR_SECOND: return "2";
        case MINOR_THIRD: return "b3";
        case MAJOR_THIRD: return "3";
        case PERFECT_FOURTH: return "4";
        case DIMINISHED_FIFTH: return "b5";
        case PERFECT_FIFTH: return "5";
        case AUGMENTED_FIFTH: return "#5";
//        case MINOR_SIXTH: return "b6";
        case MAJOR_SIXTH: return "6";
        case MINOR_SEVENTH: return "b7";
        case MAJOR_SEVENTH: return "7";
        case OCTAVE: return "8"; // !!! ??
        case OCTAVE+MINOR_SECOND: return "b9";
        case OCTAVE+MAJOR_SECOND: return "9";
        case OCTAVE+MINOR_THIRD: return "#9";
        case OCTAVE+MAJOR_THIRD: return "b11";
        case OCTAVE+PERFECT_FOURTH: return "11";
        case OCTAVE+DIMINISHED_FIFTH: return "#11";
        case OCTAVE+PERFECT_FIFTH: return "12";
        case OCTAVE+AUGMENTED_FIFTH: return "b13";
//        case OCTAVE+MINOR_SIXTH: return "b6";
        case OCTAVE+MAJOR_SIXTH: return "13";
        case OCTAVE+MINOR_SEVENTH: return "#13";
        case OCTAVE+MAJOR_SEVENTH: return "14";
        }
        return "?"+String.valueOf(interval);
    }
}