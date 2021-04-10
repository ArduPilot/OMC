/*
 * $Id: gnm_php.i e3dcd9dd0abb07d5da0e410272e0b4c5f4530411 2016-06-20 10:44:12Z Dmitry Baryshnikov $
 *
 * php specific code for gnm bindings.
 */

%init %{
  if ( OGRGetDriverCount() == 0 ) {
    OGRRegisterAll();
  }
%}

%include typemaps_php.i
