(function (window) {
	"use strict";

	if (!window || window.LacnicAjaxLoadingButton) {
		return;
	}

	var fallbackTimersByButtonId = {};

	function resolveButton(buttonId) {
		if (!buttonId) {
			return null;
		}
		return window.document.getElementById(buttonId);
	}

	function clearFallbackTimer(buttonId) {
		var timerId = fallbackTimersByButtonId[buttonId];
		if (!timerId) {
			return;
		}
		window.clearTimeout(timerId);
		delete fallbackTimersByButtonId[buttonId];
	}

	function stop(buttonId) {
		clearFallbackTimer(buttonId);
		var button = resolveButton(buttonId);
		if (!button) {
			return;
		}
		button.classList.remove("is-loading");
		button.removeAttribute("aria-busy");
		button.removeAttribute("aria-disabled");
	}

	function start(buttonId) {
		var button = resolveButton(buttonId);
		if (!button) {
			return;
		}
		clearFallbackTimer(buttonId);
		button.classList.add("is-loading");
		button.setAttribute("aria-busy", "true");
		button.setAttribute("aria-disabled", "true");
		fallbackTimersByButtonId[buttonId] = window.setTimeout(function () {
			stop(buttonId);
		}, 30000);
	}

	window.LacnicAjaxLoadingButton = {
		start: start,
		stop: stop
	};
})(window);
