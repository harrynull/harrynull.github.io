// Compile with
// Run with
// ./floyd
#include <iostream>
#include "json.hpp"
#include "../src/user/marklin/track_data_new.hpp"
#include "../src/user/marklin/track_node.hpp"
using json = nlohmann::json;


int main() {
    track_node nodes[TRACK_MAX];
    init_tracka(nodes);

    json dump;
    for (int i = 0; i < TRACK_MAX; ++i) {
        json j;
        auto &node = nodes[i];
        j["name"] = node.name;
        j["type"] = node.type;
        j["num"] = node.num;
        j["id"] = i;
        if (node.reverse) j["reverse"] = node.reverse - nodes;
        else j["reverse"] = nullptr;
        for (int k = 0; k < 2; ++k) {
            json edge;
            auto &e = node.edge[k];
            if (e.dest == nullptr) {
                j["edge"].push_back(nullptr);
                continue;
            }
            edge["src"] = e.src - nodes;
            edge["dest"] = e.dest - nodes;
            edge["dist"] = e.dist;
            j["edge"].push_back(edge);
        }
        dump.push_back(j);
    }
    std::cout << dump << std::endl;
}

