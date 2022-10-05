Name: recoil
Version: 6.3.1
Release: 1
Summary: Viewer of retro computer image files
License: GPLv2+
Group: Applications/Multimedia
Source: https://downloads.sourceforge.net/recoil/recoil-%{version}.tar.gz
URL: https://recoil.sourceforge.net/
BuildRequires: gcc, libxslt
BuildRoot: %{_tmppath}/%{name}-root

%description
Decoder of pictures in native formats of
Amiga, Amstrad CPC, Apple II, Atari 8-bit/Portfolio/ST/TT/Falcon, BBC Micro,
Commodore VIC-20/64/16/116/Plus4/128, Electronika BK, FM Towns, HP 48,
Macintosh 128K, MSX, NEC PC-80/88/98, Oric, Psion Series 3, SAM Coupe,
Sharp X68000, Tandy 1000, Timex 2048, TRS-80, TRS-80 Color Computer, ZX81
and ZX Spectrum computers.

%package 2png
Summary: Converter of retro computer image files
BuildRequires: libpng-devel

%description 2png
Provides "recoil2png" command-line converter of pictures in native formats of
Amiga, Amstrad CPC, Apple II, Atari 8-bit/Portfolio/ST/TT/Falcon, BBC Micro,
Commodore VIC-20/64/16/116/Plus4/128, Electronika BK, FM Towns, HP 48,
Macintosh 128K, MSX, NEC PC-80/88/98, Oric, Psion Series 3, SAM Coupe,
Sharp X68000, Tandy 1000, Timex 2048, TRS-80, TRS-80 Color Computer, ZX81
and ZX Spectrum computers.

%package gimp
Summary: RECOIL plugin for GIMP
Requires: gimp
BuildRequires: gimp-devel

%description gimp
GIMP plugin capable of loading pictures in native formats of
Amiga, Amstrad CPC, Apple II, Atari 8-bit/Portfolio/ST/TT/Falcon, BBC Micro,
Commodore VIC-20/64/16/116/Plus4/128, Electronika BK, FM Towns, HP 48,
Macintosh 128K, MSX, NEC PC-80/88/98, Oric, Psion Series 3, SAM Coupe,
Sharp X68000, Tandy 1000, Timex 2048, TRS-80, TRS-80 Color Computer, ZX81
and ZX Spectrum computers.

%package thumbnailer
Summary: GNOME thumbnailer for retro computer image files
Requires: recoil-2png
BuildArch: noarch

%description thumbnailer
GNOME thumbnailer for pictures in native formats of
Amiga, Amstrad CPC, Apple II, Atari 8-bit/Portfolio/ST/TT/Falcon, BBC Micro,
Commodore VIC-20/64/16/116/Plus4/128, Electronika BK, FM Towns, HP 48,
Macintosh 128K, MSX, NEC PC-80/88/98, Oric, Psion Series 3, SAM Coupe,
Sharp X68000, Tandy 1000, Timex 2048, TRS-80, TRS-80 Color Computer, ZX81
and ZX Spectrum computers.

%global debug_package %{nil}

%prep
%setup -q

%build
make recoil2png file-recoil recoil-mime.xml

%install
rm -rf $RPM_BUILD_ROOT
make PREFIX=$RPM_BUILD_ROOT/%{_prefix} libdir=$RPM_BUILD_ROOT%{_libdir} BUILDING_PACKAGE=1 install-gimp install-thumbnailer

%clean
rm -rf $RPM_BUILD_ROOT

%files 2png
%defattr(-,root,root)
%{_bindir}/recoil2png
%{_mandir}/man1/recoil2png.1*

%files gimp
%defattr(-,root,root)
%{_libdir}/gimp/2.0/plug-ins/file-recoil/file-recoil

%files thumbnailer
%defattr(-,root,root)
%{_datadir}/mime/packages/recoil-mime.xml
%{_datadir}/thumbnailers/recoil.thumbnailer

%post thumbnailer
/usr/bin/update-mime-database %{_datadir}/mime &> /dev/null || :

%postun thumbnailer
/usr/bin/update-mime-database %{_datadir}/mime &> /dev/null || :

%changelog
* Wed Oct 5 2022 Piotr Fusik <fox@scene.pl>
- 6.3.1-1

* Fri Aug 26 2022 Piotr Fusik <fox@scene.pl>
- 6.3.0-1

* Tue Jul 12 2022 Piotr Fusik <fox@scene.pl>
- 6.2.0-1

* Mon Nov 15 2021 Piotr Fusik <fox@scene.pl>
- Added GIMP plugin

* Fri Jul 16 2021 Piotr Fusik <fox@scene.pl>
- 6.1.1-1

* Sun Mar 7 2021 Piotr Fusik <fox@scene.pl>
- 6.1.0-1

* Thu Feb 4 2021 Piotr Fusik <fox@scene.pl>
- 6.0.0-1

* Fri Oct 30 2020 Piotr Fusik <fox@scene.pl>
- 5.1.1-1

* Tue Sep 8 2020 Piotr Fusik <fox@scene.pl>
- 5.1.0-1

* Wed Mar 18 2020 Piotr Fusik <fox@scene.pl>
- 5.0.1-1

* Tue Nov 12 2019 Piotr Fusik <fox@scene.pl>
- 5.0.0-1

* Sat Jul 6 2019 Piotr Fusik <fox@scene.pl>
- 4.3.2-1

* Wed Dec 5 2018 Piotr Fusik <fox@scene.pl>
- 4.3.1-1

* Sun Jun 10 2018 Piotr Fusik <fox@scene.pl>
- 4.3.0-1

* Tue Feb 20 2018 Piotr Fusik <fox@scene.pl>
- 4.2.0-1

* Wed Oct 18 2017 Piotr Fusik <fox@scene.pl>
- 4.1.0-1

* Wed May 17 2017 Piotr Fusik <fox@scene.pl>
- 4.0.0-1

* Thu Dec 1 2016 Piotr Fusik <fox@scene.pl>
- 3.5.0-1

* Tue Feb 2 2016 Piotr Fusik <fox@scene.pl>
- 3.4.0-1

* Fri Aug 7 2015 Piotr Fusik <fox@scene.pl>
- 3.3.0-1

* Wed Jan 28 2015 Piotr Fusik <fox@scene.pl>
- 3.2.0-1

* Wed Jun 25 2014 Piotr Fusik <fox@scene.pl>
- 3.1.0-1
- Initial packaging
