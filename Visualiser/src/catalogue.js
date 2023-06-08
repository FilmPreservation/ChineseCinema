var search_input = '';
//0-search by title, 1-search by filmmaker, 2-search by studio, 3-search by year and region
var search_mode = 0;

var films_inquiry = [];
var films_shown = [];
var catalogue_scroll = 0;
var max_catalogue_scroll = 0;

var cat_to_map_button, cat_to_name_button, cat_to_studio_button;

var catalogue_tag = "Showing all films in dataset:";
var catalogue_tag_chn = "显示数据集中的所有电影：";

const CATALOGUE_FILM_HEIGHT = 100;
const CATALOGUE_SLIDER_SIZE = 48;
const MAP_PREVIEW_WIDTH = 320;
const CATALOGUE_SCROLLER_WIDTH = 24;
const CATALOGUE_BOTTOM_BAR_HEIGHT = 64;

var search_button, clear_search_button;

function draw_catalogue() {
	frame_offset_y = catalogue_scroll;
	//Draw all buttons in films_shown
	fill(255);
	textAlign(LEFT, CENTER);
	textSize(24);
	text((lan === 'chn' ? catalogue_tag_chn : catalogue_tag), 24, CATALOGUE_FILM_HEIGHT / 2);

	highlited_regions.splice(0, highlited_regions.length);
	for(let i = 0; i < films_shown.length; i++) {
		films_shown[i].update();
	}
}

function draw_bottom_bar() {
	fill(0);
	rect(0, DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT, DEFAULT_WIDTH, CATALOGUE_BOTTOM_BAR_HEIGHT);
	search_button.update();
	clear_search_button.update();
	cat_to_map_button.update();
	cat_to_name_button.update();
	cat_to_studio_button.update();
}

function draw_catalogue_scroll() {
	//Update catalogue scroll
	fill(255);
	rect(width-CATALOGUE_SCROLLER_WIDTH, -64, CATALOGUE_SCROLLER_WIDTH, height+64);

	if(lan==='chn') textFont(font_chn);
	else textFont('Arial');

	fill(200);
	if(mouseX > width-CATALOGUE_SCROLLER_WIDTH && mouseY > 0 && mouseY < height) {
		if(clicked) {
			if(max_catalogue_scroll <= -DEFAULT_HEIGHT) {
				fill(128);
				catalogue_scroll = map(mouseY-CATALOGUE_SLIDER_SIZE/2, 0, height-CATALOGUE_SLIDER_SIZE, 0, max_catalogue_scroll);
				if(catalogue_scroll > 0) catalogue_scroll = 0;
				else if(catalogue_scroll < max_catalogue_scroll) catalogue_scroll = max_catalogue_scroll;
			}
		}
	}
	//Draw scroll bar
	rect(width-CATALOGUE_SCROLLER_WIDTH+2, map(catalogue_scroll, 0, max_catalogue_scroll, 0, height-CATALOGUE_SLIDER_SIZE), CATALOGUE_SCROLLER_WIDTH-4, CATALOGUE_SLIDER_SIZE, 3);
}

function show_all_films_in_catalogue() {
	films_inquiry = films_json.films;
	search_button = new SearchButton(DEFAULT_WIDTH/4+DEFAULT_WIDTH*2/64, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "搜索", "Search");
	clear_search_button = new ClearSearchButton(DEFAULT_WIDTH/4+DEFAULT_WIDTH*2/64+136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "重置", "Clear");
	cat_to_map_button = new ToMapButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*3, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "地图检索", "Map Filter");
	cat_to_name_button = new ToStaffButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*2, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "人名检索", "Staff Search");
	cat_to_studio_button = new ToStudioButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "制片厂检索", "Studio Index");
	search_field.value('');
	search_field.position(width/64, height-height/24*1.7);
	search_field.size(width/4, height/24);
	search_field.show();
	update_shown_films();
}

function search_in_catalogue(input) {
	search_input = input;
	update_shown_films();
}

function update_shown_films() {
	catalogue_scroll = 0;

	films_shown.splice(0, films_shown.length);
	let to_show = 0;
	for(let i = 0; i < films_inquiry.length; i++) {
		let f = films_inquiry[i];
		if(search_input.length > 0) {
			if(search_mode == 0) {
				//If searching by mode=0, only add those with wanted titles/tranlsated titles/keys
				if(f.title.includes(search_input) || f.translated.toUpperCase().includes(search_input.toUpperCase()) || f.key.includes(search_input)) {
					add_shown_film(f, to_show);
					to_show++;
					continue;
				}
			}else if(search_mode == 1) {
				//If searching by mode-1, only add those with wanted filmmakers/actors/actresses
				let osMap = new Map(Object.entries(f.otherStaffNameArrayWithRole));
				let otherStaff = Array.from(osMap.keys());
				if(f.directorNameArray.includes(search_input) || f.scriptwriterNameArray.includes(search_input)
					|| f.actingNameArray.includes(search_input) || otherStaff.includes(search_input)) {
					add_shown_film(f, to_show);
					to_show++;
					continue;
				}
			}else if(search_mode == 3) {
				if(f.key === 'xiao zhu yan kai') {
					//Special case: if searching Harbin (Heilongjiang) and year range includes 1959, add xiao zhu yan kai and break
					if(search_input === 'Heilongjiang' && min_year <= 1959 && max_year >= 1959) {
						add_shown_film(f, to_show);
						break;
					}
				}

				//If searching by mode-3, only add those produced in wanted year and in wanted region
				for(let j = 0; j < f.category.length; j++) {
					let compare_region = f.category[j];
					if(compare_region.includes('Shanghai')) compare_region = 'Shanghai';
					else if(compare_region === "Xi'an") compare_region = "Shaanxi";
					else if(compare_region === "Northeast") compare_region = 'Jilin';
					else if(compare_region === "Inner Mongolia") compare_region = 'Inner_Mongolia';
					if(f.year >= min_year && f.year <= max_year && compare_region.includes(search_input)) {
						add_shown_film(f, to_show);
						to_show++;
						continue;
					}
				}
			}else if(search_mode == 2) {
				//If searching by mode-2, only add those with wanted studio
				for(let j = 0; j < f.production.length; j++) {
					if(f.production[j].name === search_input) {
						add_shown_film(f, to_show);
						to_show++;
						break;
					}
				}
			}
		}else{
			//If not searching, add all
			add_shown_film(f, to_show);
			to_show++;
			continue;
		}
	}
	if(search_input.length > 0) {
		if(search_mode == 1) {
			catalogue_tag = "Showing films in which " + search_input + " participated:";
			catalogue_tag_chn = "显示 " + search_input + " 参与的电影：";
		}else if(search_mode == 2) {
			catalogue_tag = "Showing films released by " + studios.get(search_input).eng + ":";
			catalogue_tag_chn = "显示 " + studios.get(search_input).chn + " 发行的电影：";
		}else if(search_mode == 3) {
			let year_tag = min_year === max_year ? (' (' + min_year + ')') : (' (' + min_year + '-' + max_year + ')');
			catalogue_tag = "Showing films produced in " + search_input + year_tag + ":";
			catalogue_tag_chn = "显示在 " + REGIONS_CHN.get(search_input) + year_tag + " 生产的电影：";
		}else {
			catalogue_tag = "Showing films with search term: " + search_input;
			catalogue_tag_chn = "显示搜索词：" + search_input;
		}
	}else{
		catalogue_tag = "Showing all films in dataset:";
		catalogue_tag_chn = "显示数据集中的所有电影：";
	}
	max_catalogue_scroll = (films_shown.length + 1) * -CATALOGUE_FILM_HEIGHT + DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT;
	if(max_catalogue_scroll > -DEFAULT_HEIGHT) max_catalogue_scroll = -DEFAULT_HEIGHT;
}

function add_shown_film(f, row) {
	let c;
	if(f.category.length == 1 && GEO_CATEGORY_COLOURS.has(f.category[0])) {
		c = GEO_CATEGORY_COLOURS.get(f.category[0]);
	}else{
		c = DEFAULT_GEOCAT_COLOUR;
	}
	let year = ' (' + f.year + ')';
	films_shown.push(new FilmSelectButton(0, (row+1) * CATALOGUE_FILM_HEIGHT, DEFAULT_WIDTH - MAP_PREVIEW_WIDTH - CATALOGUE_SCROLLER_WIDTH, CATALOGUE_FILM_HEIGHT, null, 1, f.key, f.title.concat(year), f.translated.concat(year), c));
}