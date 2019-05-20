/* yaml操作函数，可以设置或保存起始点坐标 */
/* 依赖yaml-cpp */
#include "yaml-cpp/yaml.h"
#include <iostream>
#include <fstream>
#include <string>
#include <vector>

struct Vec3 { double x, y, z; /* etc - make sure you have overloaded operator== */ };


namespace YAML {
template<>
struct convert<Vec3> {
  static Node encode(const Vec3& rhs) {
    Node node;
    node[0]=rhs.x;
    node[1]=rhs.y;
    node[2]=rhs.z;
    //node.push_back(rhs.y);
    //node.push_back(rhs.z);
    return node;
  }

  static bool decode(const Node& node, Vec3& rhs) {
    if(!node.IsSequence() || node.size() != 3) {
      return false;
    }

    rhs.x = node[0].as<double>();
    rhs.y = node[1].as<double>();
    rhs.z = node[2].as<double>();
    return true;
  }
};
}

struct map{
	std::string image;
	float resolution;
	Vec3 origin;
	int negate;
	float occupied_thresh;
	float free_thresh;
};

struct map_locate{
	std::string image;
	float resolution;
	Vec3 origin;
	int negate;
	float occupied_thresh;
	float free_thresh;
	Vec3 last_origin;
	Vec3 origin_origin;
};

int add_last_origin(char* filename, Vec3 v){
	YAML::Node mapyaml = YAML::LoadFile(filename);
	std::cout << mapyaml["image"] << mapyaml["origin"] << std::endl;
	Vec3 origin_v;

	YAML::Node origin_old = YAML::Load("[0.0, 0.0, 0.0]");
	origin_old[0] = mapyaml["origin"][0].as<double>();
	origin_old[1] = mapyaml["origin"][1].as<double>();
	origin_old[2] = mapyaml["origin"][2].as<double>();
	mapyaml["origin_origin"] = origin_old;
	YAML::Node origin_new = YAML::Load("[0.0, 0.0, 0.0]");
	origin_new[0] = v.x;
	origin_new[1] = v.y;
	origin_new[2] = v.z;
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
int main(){
	Vec3 v;
	v.x=1.2;
	v.y=1.3;
	v.z=1.4;
	add_last_origin("map.yaml",v);
	use_origin("map.yaml","last_origin");
}
