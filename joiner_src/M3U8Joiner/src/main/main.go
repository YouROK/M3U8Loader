package main

import (
	"fmt"
	"log"
	"m3u8"
)

func main() {

	opt := m3u8.NewOptions()
	opt.TempDir = "/home/yourok/tmp/"
	//	opt.Url = "file:///home/yourok/test.m3u"
	opt.Url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
	opt.Name = "test"
	opt.OutFileDir = opt.TempDir
	opt.Threads = 20
	opt.Timeout = 20000

	m := m3u8.NewM3U8(opt)
	go func() {
		err := m.LoadList()
		if err == nil {
			m.GetList().GetList(0).IsLoad = true
			//			m.GetList().GetList(1).IsLoad = true
			fmt.Println("Load")
			err = m.Load()
			if err == nil {
				fmt.Println("Join")
				err = m.Join()
			}
		}
		if err != nil {
			fmt.Println(err)
		}
	}()

	stoped := 0
	for true {
		st := m.PollState()
		log.Println(st)
		if st.Stage == m3u8.Stoped {
			stoped++
			if stoped > 2 {
				break
			}
		}
	}
}
