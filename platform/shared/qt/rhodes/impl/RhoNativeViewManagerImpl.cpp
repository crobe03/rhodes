#include <time.h>
#include "ext/rho/rhoruby.h"
#include "common/RhodesApp.h"
#include "logging/RhoLog.h"

extern "C" {

int rho_native_view_manager_create_native_view(const char* viewtype, int tab_index, VALUE params)
{
    //TODO: rho_native_view_manager_create_native_view

    //RhoNativeViewHolder* h = getHolderByViewTypeName(viewtype);
    //if (h == NULL) {
    //    return -1;
    //}
    //NativeViewFactory* factory = h->factory;
    //NativeView* nv = factory->getNativeView(viewtype);
    //if (nv == NULL) {
    //    return -1;
    //}
    
    //RhoOpenedNativeView* opened_view = new RhoOpenedNativeView();
    //opened_view->factory_holder = h;
    //opened_view->n_view = nv;
    //opened_view->tab_index = tab_index;

    //addRhoNativeOpenedView(opened_view);

    //RhoNativeViewRunnable_OpenViewCommand* open_command = new RhoNativeViewRunnable_OpenViewCommand(opened_view);

    //RhoNativeViewUtil::executeInUIThread_WM(open_command);

    //return opened_view->id;
}

void rho_native_view_manager_navigate_native_view(int native_view_id, const char* url)
{
    //TODO: rho_native_view_manager_navigate_native_view

    //RhoOpenedNativeView* opened_view = getOpenedViewByID(native_view_id);
    //if (opened_view != NULL) {
    //    opened_view->n_view->navigate(url);
    //}
}

void rho_native_view_manager_destroy_native_view(int native_view_id)
{
    //TODO: rho_native_view_manager_destroy_native_view

    //RhoOpenedNativeView* opened_view = getOpenedViewByID(native_view_id);
    //if (opened_view != NULL) {
    //    removeRhoNativeOpenedView(opened_view);
    //    RhoNativeViewRunnable_CloseViewCommand* close_command = new RhoNativeViewRunnable_CloseViewCommand();
    //    RhoNativeViewUtil::executeInUIThread_WM(close_command);
    //}
}

} //extern "C"
