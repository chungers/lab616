;;; //cpp-ib/project.el

(ede-cpp-root-project 
 "cpp-ib"
 :file "~/lab616/cpp-ib/CMakeLists.txt"
 :include-path '("/src" 
		 "/test"
		 "/genfiles"
		 "/build/third_party/include")
 :system-include-path '("/usr/local/google")
 )
