package com.github.cat.yum.store.util;


public class VersionString implements Comparable<VersionString> {

     final private String version;

     public VersionString(String version) {
         if( version.length() == 0 )
             throw new IllegalArgumentException(
                     "\"version\" may not be zero length" );
         String temp[] = version.split("\\.");
         for( String s : temp )
         {
             if( s == null )
               throw new IllegalArgumentException(
                 "\"version\" contains a null version sub-string");
             if( s.length() == 0 )
               throw new IllegalArgumentException(
                "\"version\" contains a zero lenght version sub-string" );
             Integer.parseInt(s);
         }
         this.version = version;
     }

     @Override
     public String toString()
     {
         return this.version;
     }

     @Override
     public int compareTo(VersionString other)
     {
         if( other == this )
             return 0;
         if( other.version.equals( this.version ) )
             return 0;
         String otherVersionList[] = other.version.split("\\.");
         String thisVersionList[] = this.version.split("\\.");
         int thisIndex = 0;
         for( String otherVersion : otherVersionList )
         {
             if( thisIndex >= thisVersionList.length )
                 break;
             int thisVersionNum =
                     Integer.parseInt( thisVersionList[thisIndex] );
             int otherVersionNum = Integer.parseInt(otherVersion);
             if( thisVersionNum != otherVersionNum )
                 return (thisVersionNum - otherVersionNum < 0) ? -1 : 1;
             thisIndex++;
         }
         if( thisVersionList.length != otherVersionList.length )
             return (thisVersionList.length -
                     otherVersionList.length < 0) ? -1 : 1;
         else
             return 0;
     }


     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         compare( new VersionString("1.10.1e"), new VersionString("1.10.1e"));


     }

     private static void compare( VersionString s1, VersionString s2 ) {
         System.out.print("Compare " + s1 + " and " + s2 + " : ");
         System.out.println( s1.compareTo(s2) );
     }

     private static void construct( String s ) {
         System.out.print("Constructing \"" +s+ "\" - " );
         try {
             new VersionString( s );
         } catch( Exception ex )
             { System.out.print( ex ); }
         finally { System.out.println(""); }
     }
}
