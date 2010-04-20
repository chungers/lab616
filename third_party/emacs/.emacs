;;; .emacs

;; General formatting -- 80 character limits
(defun font-lock-width-keyword (width)
  "Return a font-lock style keyword for a string beyond width WIDTH
that uses 'font-lock-warning-face'."
  `((,(format "^%s\\(.+\\)" (make-string width ?.))
     (1 font-lock-warning-face t))))

(font-lock-add-keywords 'c-mode (font-lock-width-keyword 80))
(font-lock-add-keywords 'c++-mode (font-lock-width-keyword 80))
(font-lock-add-keywords 'java-mode (font-lock-width-keyword 80))
(font-lock-add-keywords 'python-mode (font-lock-width-keyword 80))
(custom-set-faces
   '(my-tab-face            ((((class color)) (:background "white"))) t)
   '(my-trailing-space-face ((((class color)) (:background "red"))) t)
   '(my-long-line-face ((((class color)) (:background "blue" :underline t))) t))
(add-hook 'font-lock-mode-hook
            (function
             (lambda ()
               (setq font-lock-keywords
                     (append font-lock-keywords
                          '(("\t+" (0 'my-tab-face t))
                            ("^.\\{81,\\}$" (0 'my-long-line-face t))
                           ("[ \t]+$"      (0 'my-trailing-space-face t))))))))
;; Trailing whitespaces:
(add-hook 'write-file-hooks 'maybe-delete-trailing-whitespace)

(defvar skip-whitespace-check nil
  "If non-nil, inhibits behaviour of
  `maybe-delete-trailing-whitespace', which is typically a
  write-file-hook.  This variable may be buffer-local, to permit
  extraneous whitespace on a per-file basis.")
(make-variable-buffer-local 'skip-whitespace-check)

(defun buffer-whitespace-normalized-p ()
  "Returns non-nil if the current buffer contains no tab characters
nor trailing whitespace.  This predicate is useful for determining
whether to enable automatic whitespace normalization.  Simply applying
it blindly to other people's files can cause enormously messy diffs!"
  (save-excursion
    (not  (or (progn (beginning-of-buffer)
                     (search-forward "\t" nil t))
              (progn (beginning-of-buffer)
                     (re-search-forward " +$" nil t))))))

(defun whitespace-check-find-file-hook ()
  (unless (buffer-whitespace-normalized-p)
    (message "Disabling whitespace normalization for this buffer...")
    (setq skip-whitespace-check t)))

;; Install hook so we don't accidentally normalise non-normal files.
(setq find-file-hooks
      (cons #'whitespace-check-find-file-hook find-file-hooks))

(defun toggle-whitespace-removal ()
  "Toggle the value of `skip-whitespace-check' in this buffer."
  (interactive)
  (setq skip-whitespace-check (not skip-whitespace-check))
  (message "Whitespace trimming %s"
           (if skip-whitespace-check "disabled" "enabled")))

(defun maybe-delete-trailing-whitespace ()
  "Calls `delete-trailing-whitespace' iff buffer-local variable
 skip-whitespace-check is nil.  Returns nil."
  (or skip-whitespace-check
      (delete-trailing-whitespace))
  nil)

;;; Use "%" to jump to the matching parenthesis.
(defun goto-match-paren (arg)
  "Go to the matching parenthesis if on parenthesis, otherwise insert
  the character typed."
  (interactive "p")
  (cond ((looking-at "\\s\(") (forward-list 1) (backward-char 1))
    ((looking-at "\\s\)") (forward-char 1) (backward-list 1))
    (t                    (self-insert-command (or arg 1))) ))
(global-set-key "%" `goto-match-paren)

; Google C++ styles
(load-file "~/lab616/third_party/emacs/google-c-style.el")
(require 'google-c-style)
(add-hook 'c-mode-common-hook 'google-set-c-style)
(add-hook 'c-mode-common-hook 'google-make-newline-indent)

; Loading of special modes.

; Git-Emacs
(add-to-list 'load-path "~/lab616/third_party/emacs/git-emacs")
(require 'git-emacs)

; CMake support
; Add cmake listfile names to the mode list.
(setq auto-mode-alist
        (append
	    '(("CMakeLists\\.txt\\'" . cmake-mode))
	    '(("CMakeLists\\.macros\\'" . cmake-mode))
	    '(("\\.cmake\\'" . cmake-mode))
	    '(("\\.cmk\\'" . cmake-mode))
	    auto-mode-alist))
(autoload 'cmake-mode "~/lab616/third_party/emacs/cmake-mode.el" t)

; C++ IDE
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
