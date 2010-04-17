;;; .emacs

; Add cmake listfile names to the mode list.
(setq auto-mode-alist
        (append
	    '(("CMakeLists\\.txt\\'" . cmake-mode))
	    '(("CMakeLists\\.macros\\'" . cmake-mode))
	    '(("\\.cmake\\'" . cmake-mode))
	    '(("\\.cmk\\'" . cmake-mode))
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

(defun my-cedet-hook ()
  ;; Code completion - symbol.
  (local-set-key [(control return)] 'semantic-ia-complete-symbol)

  ;; Code completion - member functions.
  (local-set-key "." 'semantic-complete-self-insert)
  (local-set-key ">" 'semantic-complete-self-insert)

  ;; C-c 'space': Displays menu for code completion.
  (local-set-key "\C-c " 'semantic-ia-complete-symbol-menu)

  ;; C-c j: Jumps to defintion of symbol under cursor.
  (local-set-key "\C-cj" 'semantic-ia-fast-jump)

  ;; C-c b: Jumps back after C-c j
  (local-set-key "\C-cb" 'semantic-mrub-switch-tag)

  ;; C-c v: Visits the declaration in header file (cursor on #define)
  (local-set-key "\C-cv" 'semantic-decoration-include-visit)

  ;; C-c p: Toggles between function declaration and actual implementation.
  (local-set-key "\C-cp" 'semantic-analyze-proto-impl-toggle)

  ;; C-c u: Finds all usage of symbol under cursor.
  (local-set-key "\C-cu" 'semantic-symref-symbol)

  ;; C-c d: Shows the documentation / function prototype of tag under cursor
  (local-set-key "\C-cd" 'semantic-ia-show-doc)

  ;; 
  (local-set-key "\C-c>" 'semantic-complete-analyze-inline)
)
(add-hook 'c-mode-common-hook 'my-cedet-hook)
