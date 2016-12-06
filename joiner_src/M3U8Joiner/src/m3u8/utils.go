package m3u8

import (
	"os"
)

func exists(path string) bool {
	_, err := os.Stat(path)
	return !os.IsNotExist(err)
}

func stat(path string) int64 {
	st, err := os.Stat(path)
	if err != nil {
		return 0
	}
	return st.Size()
}
