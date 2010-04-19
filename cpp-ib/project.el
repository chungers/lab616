;;; //cpp-ib/project.el

;;; load this with M-x 'load-file'
(ede-cpp-root-project 
 "cpp-ib"
 :file "~/lab616/cpp-ib/CMakeLists.txt"
 :include-path '("/src" 
		 "/test"
		 "/genfiles"
		 "/build/third_party/include"
		 "/build/third_party/boost/include"
		 )
 :system-include-path '("/usr/local/google")
 )
