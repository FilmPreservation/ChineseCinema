function draw_start_menu() {
	clear();
	textAlign(LEFT, TOP);
	if(lan == 'chn') {
		draw_start_menu_chn();
	} else if(lan == 'eng') {
		draw_start_menu_eng();
	}
}

function draw_start_menu_chn() {
	fill('#ED225D');
	textFont(font_chn);
	textSize(36);
	text('电影名录', 10, 50);
}

function draw_start_menu_eng() {
	fill('#ED225D');
	textFont(font_eng);
	textSize(36);
	text('Filmography', 10, 50);
}