(load-file "~/lab616/third_party/emacs/cedet-1.0pre7/common/cedet.el")
(require 'ede)
(require 'semantic-gcc)
(require 'semanticdb)
(require 'semantic-decorate-include)

(global-ede-mode 1)                       ; Enable the Project management system
(global-srecode-minor-mode 1)             ; Enable template insertion menu
(global-semanticdb-minor-mode 1)

(semantic-load-enable-code-helpers)       ; Enable prototype help and smart completion 
(semantic-load-enable-gaudy-code-helpers) ;
