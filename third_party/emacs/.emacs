;;; .emacs

; Add cmake listfile names to the mode list.
(setq auto-mode-alist
        (append
	    '(("CMakeLists\\.txt\\'" . cmake-mode))
	    '(("CMakeLists\\.macros\\'" . cmake-mode))
	    '(("\\.cmake\\'" . cmake-mode))
	    auto-mode-alist))
(autoload 'cmake-mode "~/lab616/third_party/emacs/cmake-mode.el" t)      ; CMake support

(load-file "~/lab616/third_party/emacs/google-c-style.el")               ; Google C++ styles
(require 'google-c-style)
(add-hook 'c-mode-common-hook 'google-set-c-style)
(add-hook 'c-mode-common-hook 'google-make-newline-indent)

;;; Use "%" to jump to the matching parenthesis.
(defun goto-match-paren (arg)
  "Go to the matching parenthesis if on parenthesis, otherwise insert
  the character typed."
  (interactive "p")
  (cond ((looking-at "\\s\(") (forward-list 1) (backward-char 1))
    ((looking-at "\\s\)") (forward-char 1) (backward-list 1))
    (t                    (self-insert-command (or arg 1))) ))
(global-set-key "%" `goto-match-paren)

(load-file "~/lab616/third_party/emacs/cedet-1.0pre7/common/cedet.el")   ; C++ IDE
(require 'ede)
(require 'semantic-gcc)
(require 'semanticdb)
(require 'semantic-decorate-include)

(global-ede-mode 1)                       ; Enable the Project management system
(global-srecode-minor-mode 1)             ; Enable template insertion menu
(global-semanticdb-minor-mode 1)

(semantic-load-enable-code-helpers)       ; Enable prototype help and smart completion 
(semantic-load-enable-gaudy-code-helpers) ;
