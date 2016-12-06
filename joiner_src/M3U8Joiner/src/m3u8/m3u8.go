package m3u8

import (
	"fmt"
	"log"
)

func printItem(item *Item, pref string) {
	fmt.Println(pref, item.FilePath, item.Url, item.IsLoad)
}

func printList(list *List, pref string) {
	fmt.Println(pref, "List\n", pref, list.Name)
	printItem(&list.Item, pref)

	if len(list.items) > 0 {
		fmt.Println(pref, "Items:")
		for _, i := range list.items {
			printItem(i, pref+"-")
		}
	}
	if len(list.lists) > 0 {
		fmt.Println(pref, "Lists:")
		for _, l := range list.lists {
			printList(l, pref+" ")
		}
	}
}

func Test() {
	//	opt := loader.NewHttpOpts("http://4pda.ru/pages/go/?u=http%3A%2F%2Fdevimages.apple.com%2Fiphone%2Fsamples%2Fbipbop%2Fbipbopall.m3u8&e=55225296")
	//	opt := loader.NewHttpOpts("http://4pda.ru/pages/go/?u=https%3A%2F%2Fdevimages.apple.com.edgekey.net%2Fstreaming%2Fexamples%2Fbipbop_4x3%2Fgear1%2Fprog_index.m3u8&e=55225296")
	//	opt := loader.NewHttpOpts("https://dl.dropboxusercontent.com/u/27917746/test.m3u")

	opt := NewOptions()
	opt.TempDir = "/home/yourok/tmp/"
	opt.Url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
	opt.Name = "test"
	opt.OutFileDir = opt.TempDir
	opt.Threads = 10

	m := NewM3U8(opt)
	m.LoadListNet()
	if m.list == nil {
		fmt.Println(m.lastErr)
		return
	}

	for _, l := range m.list.lists {
		l.IsLoad = true
	}
	//	m.list.lists[0].IsLoad = true

	m.Load()

	tmpind := -1
	for m.isLoading {
		if tmpind != m.loadIndex {
			log.Println(m.loadIndex, m.GetCount())
			tmpind = m.loadIndex
		}
	}
	fmt.Println(m.isLoading, m.lastErr)
	fmt.Println(m.list.lists[0].Name)
	fmt.Println(m.Join())
}
