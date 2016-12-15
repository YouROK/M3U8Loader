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
	opt.Url = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8"
	opt.Name = "test"
	opt.OutFileDir = opt.TempDir
	opt.Threads = 30
	opt.Timeout = 300000

	m := m3u8.NewM3U8(opt)
	go func() {
		err := m.LoadList()
		if err == nil {
			fmt.Println("Load")
			err = m.Load()
			if err == nil {
				fmt.Println("Join")
				err = m.Join()
				if err == nil {
					err = m.RemoveTemp()
					if err != nil {
						m.Finish()
					}
				}
			}
		}
	}()
	//	stoped := 0
	log.Println("receive state")
	sg := m3u8.Stage_LoadingList
	for true {
		st := m3u8.GetState(m)
		if st == nil {
			continue
		}
		sg = st.Stage
		log.Println(st)
		if sg < m3u8.Stage_LoadingList {
			break
		}
	}

	log.Println(sg, sg != m3u8.Stage_Error && sg != m3u8.Stage_Stoped && sg != m3u8.Stage_Finished)
	return

	for st := m3u8.GetState(m); st != nil; {
		log.Println(st)
	}
	log.Println(sg)
}
