/*
 * $Id: ogr_php.i c1ea2d15bafba12271a0865d5348c1c19134ce3c 2014-11-30 18:24:59Z Even Rouault $
 *
 * php specific code for ogr bindings.
 */

/*
 * $Log$
 * Revision 1.1  2005/09/02 16:19:23  kruland
 * Major reorganization to accommodate multiple language bindings.
 * Each language binding can define renames and supplemental code without
 * having to have a lot of conditionals in the main interface definition files.
 *
 */

%init %{
  if ( OGRGetDriverCount() == 0 ) {
    OGRRegisterAll();
  }
%}

%include typemaps_php.i
