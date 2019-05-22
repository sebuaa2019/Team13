namespace yaml_save{
	struct Vec3 { 
		double x, y, yaw; 
		Vec3(double x,double y,double yaw){
			this->x = x;
			this->y = y;
			this->yaw = yaw;
		}
	};
	int add_last_origin(char* filename, Vec3 v);
	int use_origin(char* filename,char* origin_name);
}
