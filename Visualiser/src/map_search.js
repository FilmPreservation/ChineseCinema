const MAP_SIZE = 540;
const MAP_OFFSET_X = 351;
const MAP_OFFSET_Y = 0;
const YEAR_SPACING = 36;
const YEAR_CELL_HEIGHT = 60, YEAR_CELL_WIDTH = 54;

var region_buttons = [];
var min_year = 1949, max_year = 1964;
let sel1 = 1950, sel2 = 1959;
let region_film_count_map = new Map();
let map_sc = 1;

var map_to_map_button, map_to_name_button, map_to_studio_button;
let hover_hint = '', hover_hint_chn = '';

function draw_search_map() {
	frame_offset_y = 0;

	fill(255);
	map_x = 0;
	map_y = 0;
	map_w = MAP_SIZE * 2;
	map_h = MAP_SIZE;
	image(wide_map_img, map_x, map_y, map_w, map_h);

	hover_hint = '';
	hover_hint_chn = '';
	let mouseOnMapCentre = false;
	let mouseMapX = -1, mouseMapY = -1;
	let cenX1 = map_x + MAP_OFFSET_X * map_sc;
	let cenY1 = map_y + MAP_OFFSET_Y * map_sc;
	if(cursorX >= cenX1 && cursorX <= cenX1 + MAP_SIZE) {
		if(cursorY >= cenY1 && cursorY <= cenY1 + MAP_SIZE) {
			mouseOnMapCentre = true;
			mouseMapX = (int)(parseFloat(cursorX - cenX1) / map_sc);
			mouseMapY = (int)(parseFloat(cursorY - cenY1) / map_sc);
		}
	}

	colorMode(HSB, 255);
	for([region, count] of region_film_count_map) {
		let opacity = count > 0 ? (min(255, map(count, 1, 240, 64, 255))) : 0;
		tint(255, opacity * 0.3, 255, opacity);
		if(opacity > 0) {
			let hovered = false;
			if(map_regions_map.has(region) ) {
				let img = map_regions_map.get(region);
				if(mouseOnMapCentre) {
					let c = img.get(mouseMapX, mouseMapY);
					//If not alpha>0, mouse is hovering upon this region
					if(c[3] > 0) {
						hovered = true;
						hover_hint = region;
						let year_tag = min_year === max_year ? (' (' + min_year + ')') : (' (' + min_year + '-' + max_year + ')');
						hover_hint = hover_hint.concat(year_tag + ': ' + count + ' film(s)');
						hover_hint_chn = REGIONS_CHN.get(region);
						hover_hint_chn = hover_hint_chn.concat(year_tag + '：' + count + '部电影');
					}
				}
				if(hovered && !clicked && pClicked) {
					tint(128);
					//Go back to catalogue and search with this region
					update_year_range();
					state = 1;
					search_mode = 3;
					search_in_catalogue(region);
					search_field.show();
				} else if(hovered && clicked) tint(128);
				else if(hovered) tint(192);
				image(img, cenX1, cenY1, MAP_SIZE, MAP_SIZE);
			}
		}
	}
	colorMode(RGB, 255);

	textAlign(CENTER, CENTER);
	let baseY = map_y + MAP_OFFSET_Y + MAP_SIZE;
	let cenY = (DEFAULT_HEIGHT - baseY - CATALOGUE_BOTTOM_BAR_HEIGHT) / 2 + baseY;
	let HOLE_SIZE = YEAR_CELL_WIDTH / 8;
	for(let year = 1949; year < 1967; year++) {
		let year_x = map(year, 1949, 1966, YEAR_SPACING, DEFAULT_WIDTH-YEAR_SPACING);
		let year_y = cenY;
		if(year >= min_year && year <= max_year) {
			fill(255);
			rect(year_x-YEAR_CELL_WIDTH/2, year_y-YEAR_CELL_HEIGHT/2, YEAR_CELL_WIDTH, YEAR_CELL_HEIGHT);
			fill(24);
			textSize(20);
			text(year, year_x, year_y);
		}else{
			fill(160);
			rect(year_x-YEAR_CELL_WIDTH/2, year_y-YEAR_CELL_HEIGHT/2, YEAR_CELL_WIDTH, YEAR_CELL_HEIGHT);
			fill(100);
			textSize(20);
			text(year, year_x, year_y);
		}
		let x1 = year_x - YEAR_CELL_WIDTH / 2 + 4;
		let y1 = year_y + YEAR_CELL_HEIGHT / 2 + 6;
		let y2 = year_y - YEAR_CELL_HEIGHT / 2 - 12;
		let fill1 = color(160), fill2 = color(160);

		let hovering = 0;//0-none, 1-top, 2-bottom
		//Check if cursor is over the cell
		if(cursorX >= year_x - YEAR_CELL_WIDTH / 2 && cursorX <= year_x + YEAR_CELL_WIDTH / 2) {
			if(cursorY >= year_y - YEAR_CELL_HEIGHT / 2 - 28 && cursorY <= year_y + YEAR_CELL_HEIGHT / 2 + 28) {
				if(cursorY < cenY) {
					hovering = 1;
					if(clicked && !pClicked) {
						sel1 = year;
						update_map();
					}
				}else {
					hovering = 2;
					if(clicked && !pClicked) {
						sel2 = year;
						update_map();
					}
				}
			}
		}

		if(year == sel1)
			fill1 = color(0, 255, 0);
		if(year == sel2)
			fill2 = color(255, 0, 0);
		if(hovering == 1)
			fill1 = color(0, 255, 0, 128);
		else if(hovering == 2)
			fill2 = color(255, 0, 0, 128);
		for(let i = 0; i < 8; i+=2) {
			let x2 = x1 + i * HOLE_SIZE;
			fill(fill2);
			rect(x2, y1, HOLE_SIZE, HOLE_SIZE);
			fill(fill1);
			rect(x2, y2, HOLE_SIZE, HOLE_SIZE);
		}
	}
}

function update_year_range() {
	if(sel1 > sel2) {
		min_year = sel2;
		max_year = sel1;
	}else if(sel1 < sel2) {
		min_year = sel1;
		max_year = sel2;
	}else{
		min_year = sel1;
		max_year = sel2;
	}
}

function start_search_map() {
	search_field.hide();
	map_sc = MAP_SIZE / wide_map_img.height;
	map_to_cat_button = new ToCatalogueButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*3, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "影片目录", "Film List");
	map_to_name_button = new ToStaffButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*2, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "人名检索", "Staff Search");
	map_to_studio_button = new ToStudioButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "制片厂检索", "Studio Index");
	update_map();
}

function draw_map_bottom_bar() {
	fill(200);
	rect(0, DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT, DEFAULT_WIDTH, CATALOGUE_BOTTOM_BAR_HEIGHT);
	textAlign(LEFT, CENTER);
	textSize(24);
	fill(0);
	text((lan === 'chn' ? hover_hint_chn : hover_hint), 12, DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT / 2);

	map_to_cat_button.update();
	map_to_name_button.update();
	map_to_studio_button.update();
}

function update_map() {
	update_year_range();
	region_film_count_map.clear();

	for([filmKey, film] of filmMap) {
		if(film.year < min_year || film.year > max_year) continue;

		for(let i=0; i<film.production.length; i++) {
			let region = film.production[i].category;
			if(region === 'Northeast') region = 'Jilin';
			else if(region === "Xi'an") region = 'Shaanxi';
			else if(region.includes("Shanghai")) region = 'Shanghai';
			else if(region.includes("Inner Mongolia")) region = 'Inner_Mongolia';

			if(region_film_count_map.has(region)) {
				let count = region_film_count_map.get(region);
				region_film_count_map.set(region, count + 1);
			}else{
				region_film_count_map.set(region, 1);
			}
		}
	}
}