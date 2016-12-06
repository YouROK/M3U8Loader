package m3u8

import (
	"loader"
)

func NewOptions() *Options {
	opt := new(Options)
	opt.HttpOpts = loader.NewHttpOpts("")
	return opt
}

type Options struct {
	*loader.HttpOpts
	Threads int

	Name       string // Name without dir and ext
	OutFileDir string // Dir to save out files
	TempDir    string // Temp directories
}
