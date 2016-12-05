package main

import (
	//	"M3U8Joiner"
	"fmt"
	//	"time"

	"os"

	"github.com/grafov/m3u8"
)

func main() {
	f, _ := os.Open("/home/yourok/1 - Amedia.m3u")
	//	f, _ := os.Open("/home/yourok/test.m3u8")

	l, t, _ := m3u8.DecodeFrom(f, false)
	f.Close()

	switch t {
	case m3u8.MEDIA:
		list := l.(*m3u8.MediaPlaylist)
		fmt.Println(list.Segments[0].Map)
	case m3u8.MASTER:
		list := l.(*m3u8.MasterPlaylist)
		fmt.Println(list)
	}
	return
	/*
		fmt.Println("**************Start*******************")
		opts := M3U8Joiner.NewOptions()
		opts.FileName = "/tmp/123"
		opts.TempDir = "/tmp/123.mp4.tmp/"
		opts.Threads = 10
		opts.Url = "http://4pda.ru/pages/go/?u=http%3A%2F%2Fdevimages.apple.com%2Fiphone%2Fsamples%2Fbipbop%2Fbipbopall.m3u8&e=55225296"
		opts.Timeout = 5000
		jj := M3U8Joiner.NewJoiner(opts)
		jj.LoadList()

		fmt.Println("List count:", jj.GetListCount())

		jj.SetListLoad(1)

		jj.Load()
		for jj.GetStage() != 5 {
			fmt.Println("Stage:", jj.GetStage())
			if jj.GetStage() == 2 {
				fmt.Println("Loaded", jj.Loaded(), jj.LoadedCount())
			}
			if jj.GetStage() == -1 {
				fmt.Println("Error", jj.GetError())
			}
			time.Sleep(time.Millisecond * 200)
		}
	time.Sleep(time.Second)*/
}
