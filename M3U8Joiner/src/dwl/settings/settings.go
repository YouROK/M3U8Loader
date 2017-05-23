package settings

import "runtime"

type Settings struct {
	Threads      int
	ErrorRepeat  int
	DownloadPath string

	DynamicSize bool

	Useragent string
	Cookies   string
}

func NewSettings() *Settings {
	s := new(Settings)
	s.Threads = 20
	s.ErrorRepeat = 5
	s.DynamicSize = false
	s.Useragent = "DWL/1.0.0 (" + runtime.GOOS + ")"
	return s
}
