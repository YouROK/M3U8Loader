package m3u8

import (
	"loader"
)

func NewOptions() Options {
	opt := Options{}
	return opt
}

type Options struct {
	*loader.HttpOpts
	Threads int

	Name        string // Name without dir and ext
	OutFilePath string // Dir to save out files
	TempDir     string // Temp directories
}
