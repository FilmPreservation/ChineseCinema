//Fonts
var font_chn, font_eng;
const FONT_CHN_PATH = './res/font/SourceHanSerifSC-Regular.otf';
const FONT_ENG_PATH = './res/font/SourceSans3-Regular.otf';
//Data
var films_json, names_csv;
const FILMS_JSON_PATH = '../metadata-all.json';
const NAMES_CSV_PATH = '../OCR/names-full.csv';
var filmMap = new Map();
//GeoCategory colours
const GEO_CATEGORY_COLOURS = new Map();
var DEFAULT_GEOCAT_COLOUR;
//Regions
const REGIONS = ['Anhui', 'Beijing', 'Canton', 'Gansu', 'Hubei', 'Hunan', 'Inner_Mongolia',
	'Jiangsu', 'Jilin', 'Qinghai', 'Shandong', 'Shanghai', 'Sichuan', 'Tianjin', 'Shaanxi', 'Xinjiang', 'Zhejiang', 'Heilongjiang'];
const REGIONS_CHN = new Map([['Anhui','安徽'], ['Beijing','北京'], ['Canton','广东'],
	['Gansu','甘肃'], ['Hubei','湖北'], ['Hunan','湖南'], ['Inner_Mongolia', '内蒙古'],
	['Jiangsu','江苏'], ['Jilin','吉林'], ['Qinghai','青海'], ['Shandong','山东'],
	['Shanghai','上海'], ['Sichuan','四川'], ['Tianjin','天津'], ["Shaanxi",'陕西'], ['Hong Kong','香港'],
	['Xinjiang','新疆'], ['Zhejiang','浙江'], ['Western Europe','巴黎'], ['Soviet Union','莫斯科'], ['Heilongjiang', '黑龙江']]);
//Film types
const FILM_TYPES_CHN = new Map([['Musical', '音乐剧'], ['Feature', '故事片'], ['Artistic Documentary', '艺术性的纪录片'],
	['Opera', '戏曲片'], ['Performance', '舞台表演']]);
//Images
const REGIONS_MAP_DIR = './res/regions/';
var map_regions_map = new Map();
var map_img, wide_map_img, bkg_img;
//Map of studios
class Studio {
	constructor(name, region, chn, eng) {
		this.name = name;
		this.region = region;
		this.chn = chn;
		this.eng = eng;
		this.filmCount = 1;
	}
}
var studios = new Map();
//Count loaded resources
const RESOURCE_NUM = 25;
var loaded_resource = 0;

function loadFonts() {
	font_chn = loadFont(FONT_CHN_PATH, add_loaded_source, resource_error);
	font_eng = loadFont(FONT_ENG_PATH, add_loaded_source, resource_error);
}

function loadData() {
	films_json = loadJSON(FILMS_JSON_PATH, init_film_map, resource_error);
	names_csv = loadTable(NAMES_CSV_PATH, "csv", "header", add_loaded_source, resource_error);
	GEO_CATEGORY_COLOURS.set('Shanghai (private)', color('#42bd57'));
	GEO_CATEGORY_COLOURS.set('Shanghai (state)', color('#96fb96'));
	GEO_CATEGORY_COLOURS.set('Shanghai (roc)', color('#a4e39a'));
	GEO_CATEGORY_COLOURS.set('Beijing', color('#e49eff'));
	GEO_CATEGORY_COLOURS.set('Northeast', color('#72d2ed'));
	GEO_CATEGORY_COLOURS.set('Canton', color('#ffabc8'));
	GEO_CATEGORY_COLOURS.set("Xi'an", color('#Fb9f68'));
	DEFAULT_GEOCAT_COLOUR = color('#ffffff');
}

function loadImages() {
	map_img = loadImage('./res/map.png', add_loaded_source, resource_error);
	wide_map_img = loadImage('./res/map-wide.png', add_loaded_source, resource_error);
	bkg_img = loadImage('./res/bkg.jpg', add_loaded_source, resource_error);
	for (let i = 0; i < REGIONS.length; i++) {
		let region = REGIONS[i];
		let img = loadImage(REGIONS_MAP_DIR + region + '.png', add_loaded_source, resource_error);
		map_regions_map.set(region, img);
	}
}

function init_film_map() {
	for (var i = 0; i < films_json.films.length; i++) {
		film = films_json.films[i];
		filmMap.set(film.key, film);
	}
	init_studio_list();
	add_loaded_source();
}

function add_loaded_source() {
	loaded_resource++;
	print("Loaded " + loaded_resource + " of " + RESOURCE_NUM + " resources.");
}

function resource_error() {
	print("Error loading resource.");
}

function getDarkerColour(c, fac) {
	return color(red(c) * fac, green(c) * fac, blue(c) * fac);
}

function init_studio_list() {
	for(let i=0; i<films_json.films.length; i++) {
		let new_studios = films_json.films[i].production;
		for(let j=0; j<new_studios.length; j++) {
			let studio = new_studios[j];
			if(studio.name === 'Harbin Film Studio') studio.category = 'Heilongjiang';
			if(!studios.has(studio.name)) {
				studios.set(studio.name, new Studio(studio.name, studio.category, studio.chn, studio.eng));
			}else{
				studios.get(studio.name).filmCount++;
			}
		}
	}
	//print(studios.size + " studios loaded.");
	//print(studios);
}