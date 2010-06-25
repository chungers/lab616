;;; .emacs

;; Colors
(set-face-foreground 'default "white")
(set-face-background 'default "black")
(set-face-background 'region "DarkRed")

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
