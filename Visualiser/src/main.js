//Default display size (will be adjusted according to window size)
const DEFAULT_WIDTH = 1080, DEFAULT_HEIGHT = 720;
//The ratio of the frame size to the window size
var frame_margin_ratio = 0.9;
//The ratio for scaling the frame size to fit window size
var frame_scale = 1.0;
//For translating the frame
var frame_offset_x = 0, frame_offset_y = 0;

//Mouse states
var cursorX, cursorY, pCursorX, pcursorY;
var clicked = false, pClicked = false;

//A out-of-frame slider to adjust the margin of the frame
let margin_slider;
let language_button;
let search_field;

//'chn' or 'eng'
var lan = 'eng';

//-1-loading data, 0-start menu, 1-catalogue, 2-map, 3-filmmakers, 4-studio page, 5-film page
var state = -1, pState = -1;

function preload() {
	loadFonts();
	loadData();
	loadImages();
}

function setup() {
	//Basic settings
	frameRate(30);
	smooth();
	noCursor();

	//Adapt display rate to the screen
	frame_scale = parseFloat(windowHeight) / parseFloat(DEFAULT_HEIGHT);
	let wantedWidth = (int)(DEFAULT_WIDTH * frame_scale);
	cnv = createCanvas(wantedWidth * frame_margin_ratio, windowHeight * frame_margin_ratio);

	//Init margin slider (html widget)
	margin_slider = createSlider(0.75, 0.95, frame_margin_ratio, 0.025);
	margin_slider.position(12, height);
	margin_slider.style('width', '80px');
	margin_slider.input(reset_margin_ratio);
	//Init language button (html widget)
	button = createButton('English/Chinese');
	button.position(96, height);
	button.mousePressed(switch_language);
	//Init search field
	search_field = createInput('');
	search_field.size(width/4, height/24);
	search_field.hide();

	background(0);
}

function attempt_to_search_catalogue() {
	search_in_catalogue(this.value);
}

function draw() {
	background(bkg_img);
	fill(0, 254-64);
	rect(0, 0, width, height);
	noTint();
	if(lan == 'chn') {textFont(font_chn);} else {textFont('Arial');}
	push();
		scale(frame_scale * frame_margin_ratio);
		if(state == 1 || state == 5 || state == 3 || state == 4) {
			draw_map_preview();
		}
		push();
			translate(frame_offset_x, frame_offset_y);
			textFont((lan === 'chn') ? font_chn : 'Arial');
			switch (state) {
				case -1:
					draw_loading_scene();
					if(loaded_resource >= RESOURCE_NUM) {
						state = 1;
						show_all_names_in_name_search();
						show_all_names_in_studio_search();
						start_search_map();
						show_all_films_in_catalogue();
					}
					break;
				case 1:
					draw_catalogue();
					break;
				case 2:
					draw_search_map();
					break;
				case 3:
					draw_name_search();
					break;
				case 4:
					draw_studio_page();
					break;
				case 5:
					draw_film_page();
					break;
				default:
					draw_start_menu();
					break;
			}
		pop();

		if(state == 1) {
			draw_bottom_bar();
		}else if(state == 3) {
			draw_name_preview();
			draw_name_bottom_bar();
		}else if(state == 4) {
			draw_studio_preview();
			draw_studio_bottom_bar();
		}else if(state == 2) {
			draw_map_bottom_bar();
		}
		
		pCursorX = cursorX;
		pcursorY = cursorY;
		pClicked = clicked;
		pState = state;
		cursorX = (int)(parseFloat(mouseX) / frame_scale / frame_margin_ratio - frame_offset_x);
		cursorY = (int)(parseFloat(mouseY) / frame_scale / frame_margin_ratio - frame_offset_y);
	pop();

	if(state == 1) {
		draw_catalogue_scroll();
	}else if(state == 3) {
		draw_name_scroll();
	}else if(state == 4) {
		draw_studio_scroll();
	}

	fill(255);
	circle(mouseX, mouseY, 6.0 * frame_scale);
}

function switch_language() {
	if(lan == 'chn') {
		lan = 'eng';
	} else if(lan == 'eng') {
		lan = 'chn';
	}
	if(state == 5)
		update_film_page();
}

function keyPressed() {
	/*if(keyCode == 76) {
		switch_language();
	}
	if(keyCode == 38) {
		catalogue_scroll += 50;
	}else if(keyCode == 40) {
		catalogue_scroll -= 100;
	}*/
}

function mousePressed() {
	if(mouseX < width && mouseY < height)
		clicked = true;
}

function mouseReleased() {
	clicked = false;
}

function reset_margin_ratio() {
	frame_margin_ratio = margin_slider.value();
	windowResized();
}

function windowResized() {
	frame_scale = parseFloat(windowHeight) / parseFloat(DEFAULT_HEIGHT);
	let wantedWidth = (int)(DEFAULT_WIDTH * frame_scale);
	resizeCanvas(wantedWidth * frame_margin_ratio, windowHeight * frame_margin_ratio);

	margin_slider.position(12, height);
	button.position(96, height);
	search_field.position(width/64, height-height/24*1.7);
	search_field.size(width/4, height/24);
}

function mouseWheel(event) {
	if(mouseX < width && mouseY < height) {
		if(state == 1) {
			if(max_catalogue_scroll > -DEFAULT_HEIGHT) return;
			catalogue_scroll -= event.delta * 0.5;
			if(catalogue_scroll > 0) catalogue_scroll = 0;
			else if(catalogue_scroll < max_catalogue_scroll) catalogue_scroll = max_catalogue_scroll;
			return false;
		}else if(state == 3) {
			if(max_names_scroll > -DEFAULT_HEIGHT) return;
			names_scroll -= event.delta * 0.5;
			if(names_scroll > 0) names_scroll = 0;
			else if(names_scroll < max_names_scroll) names_scroll = max_names_scroll;
			return false;
		}else if(state == 4) {
			if(max_studio_scroll > -DEFAULT_HEIGHT) return;
			studio_scroll -= event.delta * 0.5;
			if(studio_scroll > 0) studio_scroll = 0;
			else if(studio_scroll < max_studio_scroll) studio_scroll = max_studio_scroll;
			return false;
		}
	}
}