/* yaml操作函数，可以设置或保存起始点坐标 */
/* 依赖yaml-cpp */
#include "yaml-cpp/yaml.h"
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "yamlsave.h"
namespace yaml_save{
	int add_last_origin(char* filename, Vec3 v){
		YAML::Node mapyaml = YAML::LoadFile(filename);
		std::cout << mapyaml["image"] << mapyaml["origin"] << std::endl;

		YAML::Node origin_old = YAML::Load("[0.0, 0.0, 0.0]");
		origin_old[0] = mapyaml["origin"][0].as<double>();
		origin_old[1] = mapyaml["origin"][1].as<double>();
		origin_old[2] = mapyaml["origin"][2].as<double>();
		mapyaml["origin_origin"] = origin_old;
		YAML::Node origin_new = YAML::Load("[0.0, 0.0, 0.0]");
		origin_new[0] = v.x;
		origin_new[1] = v.y;
		origin_new[2] = v.yaw;
		mapyaml["last_origin"] = origin_new;
		std::ofstream fout(filename);
		fout << mapyaml;
		fout.close();
		return 0;
	}
	int use_origin(char* filename,char* origin_name){
		YAML::Node mapyaml = YAML::LoadFile(filename);
		if(mapyaml[origin_name].size() >= 3){
			YAML::Node origin_old = YAML::Load("[0.0, 0.0, 0.0]");
			origin_old[0] = mapyaml[origin_name][0].as<double>();
			origin_old[1] = mapyaml[origin_name][1].as<double>();
			origin_old[2] = mapyaml[origin_name][2].as<double>();
			mapyaml["origin"] = origin_old;
			std::ofstream fout(filename);
			fout << mapyaml;
			fout.close();
			return 0;
		}
		return -1;
	}
}
